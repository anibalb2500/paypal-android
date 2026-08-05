package com.paypal.android.api.model.serialization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenRequest(
    @SerialName("payment_source")
    val paymentSource: PaymentSource
)

@Serializable
data class PaymentSource(
    val token: Token
)

@Serializable
data class Token(
    val id: String,
    val type: String
)
