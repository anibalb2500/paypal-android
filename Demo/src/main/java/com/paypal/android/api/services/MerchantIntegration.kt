package com.paypal.android.api.services

enum class MerchantIntegration(val baseUrl: String, val clientId: String, val merchantId: String) {
    DEFAULT(
        baseUrl = "https://ppcp-mobile-demo-sandbox-87bbd7f0a27f.herokuapp.com/",
        clientId = "AQTfw2irFfemo-eWG4H5UY-b9auKihUpXQ2Engl4G1EsHJe2mkpfUv_SN3Mba0v3CfrL6Fk_ecwv9EOo",
        merchantId = "V9YP27HFNG2LW"
    ),
    // Live credentials for the `production_us` merchant (from mockmerchantapp's
    // public/scripts/config/merchants.js: paypalClientID / paypalEncryptedAccountNumber).
    // baseUrl is the same host + PPCP router + env slug that the XOSphere test app uses for
    // its production PPCP Direct API calls (NetworkConstants.SANDBOX_PROD_URL +
    // EnvironmentPath.ProdUs = "production_us"). Requires LiveRetrofitService (see
    // SDKSampleServerAPI) since mockmerchantapp's request/response shapes differ from the
    // sandbox merchant server's.
    LIVE(
        baseUrl = "https://gse-appstestbed.com/PPCP/production_us/",
        clientId = "AaGlF8u_RhsZ8c7RFv3IagEHP4qjQ9oRpZvoj2NMdCWPdJIftVTsS4mSAovJ0SGqxxeR0wiwQ2waMx1y",
        merchantId = "QHW8QGRWM58TS"
    ),
}
