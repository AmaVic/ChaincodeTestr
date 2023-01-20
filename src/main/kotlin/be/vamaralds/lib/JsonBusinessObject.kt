package be.vamaralds.lib

import org.json.JSONObject

data class JsonBusinessObject(val json: String) {
    override fun toString(): String = json
    fun <T>getAttributeValue(attributeName: String): T {
        val jsonObject = org.json.JSONObject(json)
        return jsonObject.get(attributeName) as T
    }

    fun getState(): String {
        return JSONObject(json).getJSONObject("currentState").getString("name")
    }
}