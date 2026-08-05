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
        clientId = "AYgaQtnz7wZVZM7ODhuVL16QczZLvZ14cBjuasBZZnIXH7pKLS1DoPuF-eS-Eg5PsTXv4gYkoOOrFS2J",
        merchantId = "8AYC6TF2L3D7W"
    ),
}
