package be.vamaralds.lib

import org.json.JSONArray
import org.json.JSONObject

/**
 * Resolves organization placeholders in test case payloads
 * Replaces $orgName with actual public keys from the workspace
 */
class PayloadResolver(private val workspaceManager: WorkspaceManager) {
    
    /**
     * Resolves all organization placeholders in a JSON payload string
     * Example: {"publicKey": "$org1"} becomes {"publicKey": "MFkw..."}
     */
    fun resolvePayload(payloadJson: String): String {
        val jsonObject = JSONObject(payloadJson)
        resolveJsonObject(jsonObject)
        return jsonObject.toString()
    }
    
    /**
     * Recursively resolves placeholders in a JSON object
     */
    private fun resolveJsonObject(jsonObject: JSONObject) {
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.get(key)
            
            when (value) {
                is String -> {
                    if (value.startsWith("$")) {
                        val orgName = value.substring(1)
                        try {
                            val publicKey = workspaceManager.getPublicKey(orgName)
                            jsonObject.put(key, publicKey)
                            ChaincodeApplication.logger.debug { "Resolved placeholder '$value' to public key for organization: $orgName" }
                        } catch (e: WorkspaceException) {
                            ChaincodeApplication.logger.warn { "Could not resolve placeholder '$value': ${e.message}" }
                        }
                    }
                }
                is JSONObject -> resolveJsonObject(value)
                is JSONArray -> resolveJsonArray(value)
            }
        }
    }
    
    /**
     * Recursively resolves placeholders in a JSON array
     */
    private fun resolveJsonArray(jsonArray: JSONArray) {
        for (i in 0 until jsonArray.length()) {
            val value = jsonArray.get(i)
            
            when (value) {
                is String -> {
                    if (value.startsWith("$")) {
                        val orgName = value.substring(1)
                        try {
                            val publicKey = workspaceManager.getPublicKey(orgName)
                            jsonArray.put(i, publicKey)
                            ChaincodeApplication.logger.debug { "Resolved placeholder '$value' to public key for organization: $orgName" }
                        } catch (e: WorkspaceException) {
                            ChaincodeApplication.logger.warn { "Could not resolve placeholder '$value': ${e.message}" }
                        }
                    }
                }
                is JSONObject -> resolveJsonObject(value)
                is JSONArray -> resolveJsonArray(value)
            }
        }
    }
}
