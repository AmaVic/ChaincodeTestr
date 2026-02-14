package be.vamaralds.lib

import org.json.JSONObject


data class ExpectedAttributeValue<T>(val boId: String, val attributeName: String, val expectedValue: T)
data class ExpectedState(val boId: String, val expectedStateName: String)

data class ChaincodeTestCase(
    val name: String,
    val businessEventName: String,
    val payload: String, //JSON
    val expectedAttributeValues: List<ExpectedAttributeValue<*>> = emptyList(),
    val expectedBOStates: List<ExpectedState> = emptyList(),
    val expectedToSucceed: Boolean = true,
    val thenMarkAsReady: Boolean = false,
    val submittedBy: String? = null // Organization name that submits this transaction
) {
    companion object {
        fun fromJson(json: String): ChaincodeTestCase {
            val jsonObject = JSONObject(json)
            val name = jsonObject.getString("name")
            val businessEventName = jsonObject.getString("businessEventName")
            val payload = jsonObject.getJSONObject("payload").toString()
            val thenMarkAsReady = jsonObject.getBoolean("thenMarkAsReady")
            val expectedAttributeValues = jsonObject.getJSONArray("expectedAttributeValues").map { expectedAttributeValue ->
                val expectedAttributeValueJson = expectedAttributeValue as JSONObject
                val boId = expectedAttributeValueJson.getString("boId")
                val attributeName = expectedAttributeValueJson.getString("attributeName")
                val expectedValue = expectedAttributeValueJson.get("expectedValue")
                ExpectedAttributeValue(boId, attributeName, expectedValue)
            }
            val expectedBOStates = jsonObject.getJSONArray("expectedBOStates").map { expectedState ->
                val expectedStateJson = expectedState as JSONObject
                val boId = expectedStateJson.getString("boId")
                val expectedStateName = expectedStateJson.getString("expectedStateName")
                ExpectedState(boId, expectedStateName)
            }
            val expectedToSucceed = jsonObject.getBoolean("expectedToSucceed")
            val submittedBy = if (jsonObject.has("submittedBy")) jsonObject.optString("submittedBy") else null
            return ChaincodeTestCase(name, businessEventName, payload, expectedAttributeValues, expectedBOStates, expectedToSucceed, thenMarkAsReady, submittedBy)
        }
    }
}

sealed interface ChaincodeTestResult {
    val testCase: ChaincodeTestCase
    fun toJsonString(): String = JSONObject(this).toString()
}

data class SuccessfulChaincodeTestResult(
    override val testCase: ChaincodeTestCase,
    val validatedObjects: Map<String, JsonBusinessObject> = emptyMap()
): ChaincodeTestResult

data class FailedChaincodeTestResult(
    override val testCase: ChaincodeTestCase,
    val reasons: List<String> = emptyList()
): ChaincodeTestResult

