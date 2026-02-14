package be.vamaralds.lib

/**
 * Manages multiple connections to the network, one for each organization
 * This allows switching between organizations when submitting transactions
 */
class MultiOrgConnectionManager(
    private val workspaceManager: WorkspaceManager,
    private val chaincodeName: String
) {
    private val connections = mutableMapOf<String, Connection>()
    private var channelName: String? = null
    
    /**
     * Gets or creates a connection for a specific organization
     */
    fun getConnection(orgName: String): Connection {
        // Validate organization exists
        workspaceManager.validateOrganization(orgName)
        
        // Return existing connection if available
        if (connections.containsKey(orgName)) {
            return connections[orgName]!!
        }
        
        // Create new connection
        ChaincodeApplication.logger.debug { "Creating connection for organization: $orgName" }
        
        val config = ConnectionConfiguration(
            walletPath = workspaceManager.getWalletPath(orgName),
            identityName = workspaceManager.getIdentityName(orgName),
            connectionProfilePath = workspaceManager.getConnectionProfilePath(orgName),
            channelName = getChannelName(orgName),
            chaincodeName = chaincodeName
        )
        
        val connection = ChaincodeTestr(config).connect()
        connections[orgName] = connection
        
        ChaincodeApplication.logger.debug { "Successfully created connection for organization: $orgName" }
        return connection
    }
    
    /**
     * Gets the channel name (discovers on first call, then caches)
     */
    private fun getChannelName(orgName: String): String {
        if (channelName == null) {
            channelName = workspaceManager.getChannelName(orgName)
            ChaincodeApplication.logger.debug { "Discovered channel name: $channelName" }
        }
        return channelName!!
    }
    
    /**
     * Gets all available organizations
     */
    fun getAvailableOrganizations(): List<String> {
        return workspaceManager.getOrganizations()
    }
    
    /**
     * Closes all open connections
     */
    fun closeAll() {
        ChaincodeApplication.logger.info { "Closing all connections (${connections.size} organizations)" }
        connections.values.forEach { connection ->
            try {
                connection.gateway.close()
            } catch (e: Exception) {
                ChaincodeApplication.logger.warn { "Error closing connection: ${e.message}" }
            }
        }
        connections.clear()
    }
}
