package be.vamaralds.lib

data class ChaincodeTestSuiteResult(
    val testSuiteName: String,
    val results: Map<ChaincodeTestCase, ChaincodeTestResult> = emptyMap()
) {
    val successful: Boolean = results.isEmpty() || results.values.all { it is SuccessfulChaincodeTestResult }
}

data class ChaincodeTestSuite(val name: String, val testsCases: List<ChaincodeTestCase> = emptyList()) {
    companion object {
        fun fromJson(json: String): ChaincodeTestSuite {
            val jsonObject = org.json.JSONObject(json)
            val name = jsonObject.getString("name")
            val testCases = jsonObject.getJSONArray("testsCases").map { testCase ->
                ChaincodeTestCase.fromJson(testCase.toString())
            }
            return ChaincodeTestSuite(name, testCases)
        }
    }
}