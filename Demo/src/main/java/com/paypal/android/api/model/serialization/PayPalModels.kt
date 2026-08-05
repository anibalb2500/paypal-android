package com.paypal.android.api.model.serialization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// PayPal Setup Request
@Serializable
data class PayPalSetupRequestBody(
    @SerialName("payment_source")
    val paymentSource: PayPalSource
)

@Serializable
data class PayPalSource(
    val paypal: PayPalDetails
)

@Serializable
data class PayPalDetails(
    @SerialName("usage_type")
    val usageType: String,
    @SerialName("experience_context")
    val experienceContext: PayPalExperienceContext
)

@Serializable
data class PayPalExperienceContext(
    @SerialName("vault_instruction")
    val vaultInstruction: String,
    @SerialName("return_url")
    val returnUrl: String,
    @SerialName("cancel_url")
    val cancelUrl: String,
    @SerialName("native_app")
    val nativeApp: PayPalNativeApp? = null
)

@Serializable
data class PayPalNativeApp(
    @SerialName("app_url")
    val appUrl: String
)
