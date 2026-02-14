package be.vamaralds.lib

import org.json.JSONObject
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class WorkspaceException(message: String) : Exception(message)

/**
 * Manages discovery and access to organization credentials in an easycc workspace
 */
class WorkspaceManager(private val workspacePath: String) {
    
    private val exportsPath = Paths.get(workspacePath, "exports")
    private val walletsPath = exportsPath.resolve("wallets")
    private val connectionProfilesPath = exportsPath.resolve("connection-profiles")
    
    init {
        if (!Files.exists(exportsPath)) {
            throw WorkspaceException("Workspace exports directory not found at: $exportsPath")
        }
        if (!Files.exists(walletsPath)) {
            throw WorkspaceException("Wallets directory not found at: $walletsPath")
        }
        if (!Files.exists(connectionProfilesPath)) {
            throw WorkspaceException("Connection profiles directory not found at: $connectionProfilesPath")
        }
    }
    
    /**
     * Discovers all available organizations in the workspace
     */
    fun getOrganizations(): List<String> {
        val walletsDir = walletsPath.toFile()
        return walletsDir.listFiles()
            ?.filter { it.isDirectory && !it.name.startsWith(".") }
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()
    }
    
    /**
     * Gets the wallet path for a specific organization
     */
    fun getWalletPath(orgName: String): String {
        val walletPath = walletsPath.resolve(orgName)
        if (!Files.exists(walletPath)) {
            throw WorkspaceException("Wallet not found for organization: $orgName at $walletPath")
        }
        return walletPath.toAbsolutePath().toString()
    }
    
    /**
     * Gets the connection profile path for a specific organization
     */
    fun getConnectionProfilePath(orgName: String): String {
        val profilePath = connectionProfilesPath.resolve("connection-${orgName}.json")
        if (!Files.exists(profilePath)) {
            throw WorkspaceException("Connection profile not found for organization: $orgName at $profilePath")
        }
        return profilePath.toAbsolutePath().toString()
    }
    
    /**
     * Gets the identity name for a specific organization (always "admin")
     */
    fun getIdentityName(orgName: String): String {
        return "admin"
    }
    
    /**
     * Gets the public key for a specific organization from public_key.txt
     */
    fun getPublicKey(orgName: String): String {
        val publicKeyPath = walletsPath.resolve(orgName).resolve("public_key.txt")
        if (!Files.exists(publicKeyPath)) {
            throw WorkspaceException("Public key file not found for organization: $orgName at $publicKeyPath")
        }
        return publicKeyPath.toFile().readText().trim()
    }
    
    /**
     * Gets the channel name from a connection profile
     * Returns the first channel found, or "mychannel" as default
     */
    fun getChannelName(orgName: String): String {
        try {
            val profilePath = getConnectionProfilePath(orgName)
            val profileContent = File(profilePath).readText()
            val jsonObject = JSONObject(profileContent)
            
            if (jsonObject.has("channels")) {
                val channels = jsonObject.getJSONObject("channels")
                val channelNames = channels.keys()
                if (channelNames.hasNext()) {
                    return channelNames.next()
                }
            }
        } catch (e: Exception) {
            ChaincodeApplication.logger.warn { "Could not discover channel name from connection profile: ${e.message}" }
        }
        
        // Default fallback
        return "mychannel"
    }
    
    /**
     * Validates that an organization exists in the workspace
     */
    fun validateOrganization(orgName: String) {
        val orgs = getOrganizations()
        if (!orgs.contains(orgName)) {
            throw WorkspaceException(
                "Organization '$orgName' not found in workspace. Available organizations: ${orgs.joinToString(", ")}"
            )
        }
    }
}
