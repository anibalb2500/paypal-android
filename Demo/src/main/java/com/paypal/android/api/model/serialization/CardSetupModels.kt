package com.paypal.android.api.model.serialization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CardSetupRequest(
    @SerialName("payment_source")
    val paymentSource: CardPaymentSource
)

@Serializable
data class CardPaymentSource(
    val card: CardDetails
)

@Serializable
data class CardDetails(
    @SerialName("verification_method")
    val verificationMethod: String,
    @SerialName("experience_context")
    val experienceContext: ExperienceContext
)

@Serializable
data class ExperienceContext(
    @SerialName("return_url")
    val returnUrl: String,
    @SerialName("cancel_url")
    val cancelUrl: String
)
