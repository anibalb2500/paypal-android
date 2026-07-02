@file:Suppress("SpacingAroundParens", "NoMultipleSpaces", "MaxLineLength")

package com.paypal.android.paypalwebpayments.analytics

internal enum class CreatePayPalSessionEvent(val value: String) {
    // @formatter:off
    STARTED(  "paypal-web-payments:create-paypal-session:started"),
    SUCCEEDED("paypal-web-payments:create-paypal-session:succeeded"),
    FAILED(   "paypal-web-payments:create-paypal-session:failed"),
    // @formatter:on
}
