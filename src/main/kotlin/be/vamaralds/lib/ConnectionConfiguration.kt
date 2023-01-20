package be.vamaralds.lib

data class ConnectionConfiguration(
    val walletPath: String,
    val identityName: String,
    val connectionProfilePath: String,
    val channelName: String = "mychannel",
    val chaincodeName: String
): JsonSerializable {
    override fun toString(): String = toJsonString()
}