package com.paypal.android.api.services

import com.paypal.android.api.model.Order
import com.paypal.android.api.model.PayPalSetupToken
import com.paypal.android.api.model.serialization.CardSetupRequest
import com.paypal.android.api.model.serialization.LiveCaptureOrderResponse
import com.paypal.android.api.model.serialization.LiveCreateOrderResponse
import com.paypal.android.api.model.serialization.LiveCreateSetupTokenResponse
import com.paypal.android.api.model.serialization.LivePaymentTokenResponse
import com.paypal.android.api.model.serialization.OrderRequestBody
import com.paypal.android.api.model.serialization.OrderResponse
import com.paypal.android.api.model.serialization.PayPalSetupRequestBody
import com.paypal.android.api.model.serialization.PaymentTokenResponse
import com.paypal.android.api.model.serialization.SetupTokenResponse
import com.paypal.android.api.model.serialization.TokenRequest
import com.paypal.android.api.model.serialization.toCardPaymentToken
import com.paypal.android.api.model.serialization.toCardSetupToken
import com.paypal.android.api.model.serialization.toOrder
import com.paypal.android.api.model.serialization.toPayPalPaymentToken
import com.paypal.android.api.model.serialization.toPayPalSetupToken
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

private const val CONNECT_TIMEOUT_IN_SEC = 20L
private const val READ_TIMEOUT_IN_SEC = 30L
private const val WRITE_TIMEOUT_IN_SEC = 30L

// To hardcode an orderId for this demo app, set the below value
private val DEFAULT_ORDER_ID: String? = null // = "your-order-id"

// TODO: consider refactoring each method into a "use case"
// Ref: https://developer.android.com/topic/architecture/domain-layer#use-cases-kotlin
@Suppress("TooManyFunctions")
class SDKSampleServerAPI(
    /** Overrides [MerchantIntegration.DEFAULT]'s base URL when non-null and non-blank. */
    private val customMerchantBaseUrl: String? = null
) {

    companion object {
        // TODO: - require Merchant enum to be specified via UI layer
        val SELECTED_MERCHANT_INTEGRATION = MerchantIntegration.DEFAULT

        val clientId: String
            get() = SELECTED_MERCHANT_INTEGRATION.clientId

        val merchantId: String
            get() = SELECTED_MERCHANT_INTEGRATION.merchantId
    }

    /** Route shape used by the sandbox merchant server (sdk-sample-merchant-server). */
    @JvmSuppressWildcards
    interface RetrofitService {

        @POST("/orders")
        suspend fun createOrder(@Body orderRequestBody: OrderRequestBody): Order

        @POST("/orders/{orderId}/capture")
        suspend fun captureOrder(
            @Path("orderId") orderId: String,
            @Header("PayPal-Client-Metadata-Id") payPalClientMetadataId: String?
        ): OrderResponse

        @POST("/orders/{orderId}/authorize")
        suspend fun authorizeOrder(
            @Path("orderId") orderId: String,
            @Header("PayPal-Client-Metadata-Id") payPalClientMetadataId: String?
        ): OrderResponse

        @POST("/setup-tokens")
        suspend fun createSetupToken(@Body setupRequest: CardSetupRequest): SetupTokenResponse

        @POST("/setup-tokens")
        suspend fun createPayPalSetupToken(@Body setupRequest: PayPalSetupRequestBody): SetupTokenResponse

        @POST("/payment-tokens")
        suspend fun createPaymentToken(@Body tokenRequest: TokenRequest): PaymentTokenResponse

        @GET("/setup-tokens/{setupTokenId}")
        suspend fun getSetupToken(
            @Path("setupTokenId") setupTokenId: String,
        ): SetupTokenResponse
    }

    /**
     * Route shape used by the live merchant server (mockmerchantapp, aka XOSphere's PPCP
     * Direct API integration). Paths are relative to [MerchantIntegration.LIVE]'s baseUrl,
     * which already includes the `/PPCP/production_us/` prefix. Response bodies are
     * mockmerchantapp's own reshaped DTOs, not raw PayPal Orders v2 / Vault v3 JSON -- see
     * [com.paypal.android.api.model.serialization.LiveCreateOrderResponse] and friends.
     *
     * There's no vault-only endpoint that supports card payment sources here (mockmerchantapp's
     * vault routes assume a PayPal payment source), and no PayPal-Client-Metadata-Id forwarding
     * on capture/authorize -- both are sandbox-only capabilities for now.
     */
    @JvmSuppressWildcards
    interface LiveRetrofitService {

        @POST("v2/checkout/orders")
        suspend fun createOrder(@Body orderRequestBody: OrderRequestBody): LiveCreateOrderResponse

        @POST("v2/checkout/orders/{orderId}/capture")
        suspend fun captureOrder(@Path("orderId") orderId: String): LiveCaptureOrderResponse

        @POST("v2/checkout/orders/{orderId}/authorize")
        suspend fun authorizeOrder(@Path("orderId") orderId: String): LiveCaptureOrderResponse

        @POST("v3/vault/setup-tokens")
        suspend fun createPayPalSetupToken(
            @Body setupRequest: PayPalSetupRequestBody
        ): LiveCreateSetupTokenResponse

        @POST("v3/vault/payment-tokens")
        suspend fun createPaymentToken(@Body tokenRequest: TokenRequest): LivePaymentTokenResponse
    }

    /** Wraps whichever Retrofit service shape a given [MerchantIntegration] actually speaks. */
    private sealed class MerchantService {
        data class Default(val service: RetrofitService) : MerchantService()
        data class Live(val service: LiveRetrofitService) : MerchantService()
    }

    private val serviceMap: Map<MerchantIntegration, MerchantService>

    init {
        val serviceMap = mutableMapOf<MerchantIntegration, MerchantService>()
        for (merchant in MerchantIntegration.entries) {
            val baseUrl = if (merchant == MerchantIntegration.DEFAULT && !customMerchantBaseUrl.isNullOrBlank()) {
                customMerchantBaseUrl
            } else {
                merchant.baseUrl
            }
            serviceMap[merchant] = if (merchant == MerchantIntegration.LIVE) {
                MerchantService.Live(createLiveService(baseUrl))
            } else {
                MerchantService.Default(createService(baseUrl))
            }
        }
        this.serviceMap = serviceMap
    }

    private fun buildRetrofit(baseUrl: String): Retrofit {
        val okHttpBuilder = OkHttpClient.Builder()
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        // Timeouts
        okHttpBuilder
            .connectTimeout(CONNECT_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
        okHttpBuilder.addInterceptor(httpLoggingInterceptor)
        val okHttpClient = okHttpBuilder.build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(KotlinSerializationConverterFactory.create())
            .build()
    }

    private fun createService(baseUrl: String): RetrofitService =
        buildRetrofit(baseUrl).create(RetrofitService::class.java)

    private fun createLiveService(baseUrl: String): LiveRetrofitService =
        buildRetrofit(baseUrl).create(LiveRetrofitService::class.java)

    private fun findMerchantService(merchantIntegration: MerchantIntegration) =
        serviceMap[merchantIntegration]
            ?: throw AssertionError("Couldn't find retrofit service for ${merchantIntegration.name}")

    private fun unsupportedForLive(operation: String): Nothing = throw UnsupportedOperationException(
        "$operation is not supported against MerchantIntegration.LIVE: mockmerchantapp's " +
            "vault endpoints assume a PayPal payment source, not a card."
    )

    suspend fun createOrder(
        orderRequestBody: OrderRequestBody,
        merchantIntegration: MerchantIntegration = SELECTED_MERCHANT_INTEGRATION
    ) = safeApiCall {
        if (DEFAULT_ORDER_ID != null) {
            Order(DEFAULT_ORDER_ID, "CREATED")
        } else {
            when (val service = findMerchantService(merchantIntegration)) {
                is MerchantService.Default -> service.service.createOrder(orderRequestBody)
                is MerchantService.Live -> service.service.createOrder(orderRequestBody).toOrder()
            }
        }
    }

    suspend fun captureOrder(
        orderId: String,
        payPalClientMetadataId: String? = null,
        merchantIntegration: MerchantIntegration = SELECTED_MERCHANT_INTEGRATION
    ) = safeApiCall {
        when (val service = findMerchantService(merchantIntegration)) {
            is MerchantService.Default ->
                service.service.captureOrder(orderId, payPalClientMetadataId).toOrder()
            is MerchantService.Live ->
                service.service.captureOrder(orderId).toOrder(orderId)
        }
    }

    suspend fun authorizeOrder(
        orderId: String,
        payPalClientMetadataId: String? = null,
        merchantIntegration: MerchantIntegration = SELECTED_MERCHANT_INTEGRATION
    ) = safeApiCall {
        when (val service = findMerchantService(merchantIntegration)) {
            is MerchantService.Default ->
                service.service.authorizeOrder(orderId, payPalClientMetadataId).toOrder()
            is MerchantService.Live ->
                service.service.authorizeOrder(orderId).toOrder(orderId)
        }
    }

    /** Card vaulting. Not available against [MerchantIntegration.LIVE]; see [unsupportedForLive]. */
    suspend fun createSetupToken(
        setupRequest: CardSetupRequest,
        merchantIntegration: MerchantIntegration = SELECTED_MERCHANT_INTEGRATION
    ) = safeApiCall {
        when (val service = findMerchantService(merchantIntegration)) {
            is MerchantService.Default -> service.service.createSetupToken(setupRequest).toCardSetupToken()
            is MerchantService.Live -> unsupportedForLive("Card vaulting (createSetupToken)")
        }
    }

    /** Card vaulting. Not available against [MerchantIntegration.LIVE]; see [unsupportedForLive]. */
    suspend fun getSetupToken(
        setupTokenId: String,
        merchantIntegration: MerchantIntegration = SELECTED_MERCHANT_INTEGRATION
    ) = safeApiCall {
        when (val service = findMerchantService(merchantIntegration)) {
            is MerchantService.Default -> service.service.getSetupToken(setupTokenId).toCardSetupToken()
            is MerchantService.Live -> unsupportedForLive("getSetupToken")
        }
    }

    /** Card vaulting. Not available against [MerchantIntegration.LIVE]; see [unsupportedForLive]. */
    suspend fun createPaymentToken(
        tokenRequest: TokenRequest,
        merchantIntegration: MerchantIntegration = SELECTED_MERCHANT_INTEGRATION
    ) = safeApiCall {
        when (val service = findMerchantService(merchantIntegration)) {
            is MerchantService.Default -> service.service.createPaymentToken(tokenRequest).toCardPaymentToken()
            is MerchantService.Live -> unsupportedForLive("Card vaulting (createPaymentToken)")
        }
    }

    suspend fun createPayPalPaymentToken(
        tokenRequest: TokenRequest,
        merchantIntegration: MerchantIntegration = SELECTED_MERCHANT_INTEGRATION
    ) = safeApiCall {
        when (val service = findMerchantService(merchantIntegration)) {
            is MerchantService.Default ->
                service.service.createPaymentToken(tokenRequest).toPayPalPaymentToken()
            is MerchantService.Live ->
                service.service.createPaymentToken(tokenRequest).toPayPalPaymentToken()
        }
    }

    suspend fun createPayPalSetupToken(
        setupRequest: PayPalSetupRequestBody,
        merchantIntegration: MerchantIntegration = SELECTED_MERCHANT_INTEGRATION
    ) = safeApiCall {
        when (val service = findMerchantService(merchantIntegration)) {
            is MerchantService.Default -> {
                val setupTokenResponse = service.service.createPayPalSetupToken(setupRequest)
                PayPalSetupToken(
                    id = setupTokenResponse.id,
                    customerId = setupTokenResponse.customer.id,
                    status = setupTokenResponse.status
                )
            }
            is MerchantService.Live ->
                service.service.createPayPalSetupToken(setupRequest).toPayPalSetupToken()
        }
    }

    // Ref: https://medium.com/@douglas.iacovelli/how-to-handle-errors-with-retrofit-and-coroutines-33e7492a912
    @Suppress("TooGenericExceptionCaught")
    private suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): SDKSampleServerResult<T, SDKSampleServerException> = try {
        SDKSampleServerResult.Success(apiCall.invoke())
    } catch (e: Throwable) {
        SDKSampleServerResult.Failure(SDKSampleServerException(e.message, e))
    }
}
