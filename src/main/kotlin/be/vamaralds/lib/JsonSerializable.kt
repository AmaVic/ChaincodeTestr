package be.vamaralds.lib

import org.json.JSONObject

interface JsonSerializable {
    fun toJson(): JSONObject = JSONObject(this)
    fun toJsonString(): String = toJson().toString()
}