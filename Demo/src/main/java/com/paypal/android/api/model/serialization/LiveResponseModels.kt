package com.paypal.android.api.model.serialization

import com.paypal.android.api.model.Order
import com.paypal.android.api.model.PayPalPaymentToken
import com.paypal.android.api.model.PayPalSetupToken
import kotlinx.serialization.Serializable

// Response shapes returned by mockmerchantapp's PPCP Direct API routes (used for
// MerchantIntegration.LIVE). Unlike the sandbox merchant server, mockmerchantapp does not pass
// through PayPal's raw Orders v2 / Vault v3 responses -- it reshapes them into its own small
// DTOs (see server/src/utils/ppcp/one-time-checkout.js and vault.js in the mockmerchantapp
// repo). These fields are already camelCase JS object literals from that server, so no
// @SerialName mapping is needed here.

@Serializable
data class LiveCreateOrderResponse(
    val id: String? = null,
    val appSwitchEligibility: Boolean? = null,
    val redirectUrl: String? = null,
    val debugId: String? = null
)

@Serializable
data class LiveCaptureOrderResponse(
    val status: String? = null,
    val transactionId: String? = null,
    val transactionDebugId: String? = null,
    val vaultId: String? = null
)

@Serializable
data class LiveCreateSetupTokenResponse(
    val id: String? = null,
    val appSwitchEligibility: Boolean? = null,
    val redirectUrl: String? = null,
    val debugId: String? = null
)

@Serializable
data class LivePaymentTokenResponse(
    val id: String? = null,
    val email: String? = null,
    val payerId: String? = null,
    val givenName: String? = null,
    val debugId: String? = null
)

// Mapping functions into the app's existing domain models, so ViewModels/UseCases don't need
// to know or care which merchant backend actually served the request.

fun LiveCreateOrderResponse.toOrder(): Order = Order(
    id = id,
    // mockmerchantapp's create-order response doesn't include intent/status; unavailable here.
    intent = null,
    status = null
)

fun LiveCaptureOrderResponse.toOrder(orderId: String): Order = Order(
    id = orderId,
    intent = null,
    status = status,
    vaultId = vaultId
)

fun LiveCreateSetupTokenResponse.toPayPalSetupToken(): PayPalSetupToken = PayPalSetupToken(
    id = id.orEmpty(),
    // mockmerchantapp's create-setup-token response doesn't include a customer id or status;
    // only `.id` is used downstream (PayPalClient#vault), so these are safe placeholders.
    customerId = "",
    status = "CREATED"
)

fun LivePaymentTokenResponse.toPayPalPaymentToken(): PayPalPaymentToken = PayPalPaymentToken(
    id = id.orEmpty(),
    // mockmerchantapp returns the PayPal payer_id, not a Vault v3 "customer" id; closest
    // available identifier and not used to drive any further calls in this app.
    customerId = payerId.orEmpty()
)
