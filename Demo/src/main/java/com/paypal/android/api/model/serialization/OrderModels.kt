package com.paypal.android.api.model.serialization

import com.paypal.android.api.model.OrderIntent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// NOTE: explicit snake_case @SerialName mappings below match PayPal's real Orders v2 API
// field names. The sandbox merchant server (sdk-sample-merchant-server) used to paper over
// this by snake-casing the request body itself before forwarding to PayPal; the LIVE
// merchant server (mockmerchantapp) forwards the body unchanged, so the client must already
// send the correct casing. Adding these annotations is a no-op for the sandbox path (its
// snake-case conversion is idempotent on already-snake_case keys) and required for LIVE.
@Serializable
data class OrderRequestBody(
    val intent: OrderIntent,
    @SerialName("purchase_units")
    val purchaseUnits: List<PurchaseUnit>,
    @SerialName("payment_source")
    val paymentSource: OrderPaymentSource? = null
)

@Serializable
data class PurchaseUnit(
    val amount: Amount
)

@Serializable
data class Amount(
    @SerialName("currency_code")
    val currencyCode: String,
    val value: String
)

@Serializable
data class OrderPaymentSource(
    val card: Card? = null,
    val paypal: PayPalPaymentSource? = null
)

@Serializable
data class Card(
    val attributes: CardAttributes
)

@Serializable
data class CardAttributes(
    val vault: Vault
)

@Serializable
data class Vault(
    @SerialName("store_in_vault")
    val storeInVault: String,
    @SerialName("usage_type")
    val usageType: String? = null,
    @SerialName("customer_type")
    val customerType: String? = null
)

@Serializable
data class PayPalPaymentSource(
    val attributes: PayPalAttributes? = null,
    @SerialName("experience_context")
    val experienceContext: PayPalOrderExperienceContext? = null
)

@Serializable
data class PayPalAttributes(
    val vault: Vault
)

@Serializable
data class PayPalOrderExperienceContext(
    @SerialName("return_url")
    val returnUrl: String,
    @SerialName("cancel_url")
    val cancelUrl: String,
    @SerialName("native_app")
    val nativeApp: NativeApp? = null
)

@Serializable
data class NativeApp(
    @SerialName("app_url")
    val appUrl: String
)
