package be.vamaralds.lib

import org.hyperledger.fabric.gateway.*
import java.io.IOException
import java.lang.IllegalArgumentException
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.jvm.Throws

open class ChaincodeTestrException(msg: String): Exception(msg)
class IncorrectWalletPathException(msg: String): ChaincodeTestrException(msg)

class ChaincodeTestr(val config: ConnectionConfiguration) {
    @Throws(ChaincodeTestrException::class)
    fun connect(): Connection {
        ChaincodeApplication.logger.info { "Initiating Connection to the HLF network" }
        //Retrieving Wallet to Interact with the Network
        val walletPath = Paths.get(config.walletPath)
        var wallet: Wallet?

        try {
            wallet = Wallets.newFileSystemWallet(walletPath)
            ChaincodeApplication.logger.info { "Retrieving Connection Wallet: Successful" }
        } catch (e: Exception) {
            val error = IncorrectWalletPathException("Failed to read wallet in folder: $walletPath. Make sure that the wallet path is correct")
            ChaincodeApplication.logger.error(error) { "Retrieving Connection Wallet: Failed: $error" }
            throw error
        }

        //Retrieving Network lib.Connection Profile Path
        val connectionProfilePath = Paths.get(config.connectionProfilePath)

        //Create Gateway Builder
        var gatewayBuilder: Gateway.Builder?
        try {
            gatewayBuilder = Gateway.createBuilder()
                .identity(wallet, config.identityName) //Org1 Admin for organization1, Org2 Admin for the second org.
                .networkConfig(connectionProfilePath)
                .commitTimeout(1, TimeUnit.MINUTES)
        } catch(e: IllegalArgumentException) {
            val error = ChaincodeTestrException("Failed to create Gateway Builder: $e")
            throw error
        } catch(e: IOException) {
            throw ChaincodeTestrException("Failed to create Gateway builder due to an issue with connection profile file: ${e.toString()}")
        }

        //lib.Connection to Network through Gateway
        var gateway: Gateway?
        try {
            gateway = gatewayBuilder.connect()
            ChaincodeApplication.logger.info { "Connection to the HLF network: Successful" }
        } catch(e: Exception) {
            val error = ChaincodeTestrException("Failed to connect to the network: ${e.javaClass.simpleName}: ${e.message}")
            throw error
        }

        var network: Network?
        var contract: Contract?

        try {
            network = gateway.getNetwork(config.channelName)
            ChaincodeApplication.logger.info { "Selecting Channel ${config.channelName}: Successful" }
        } catch(e: Exception) {
            val error = ChaincodeTestrException("Failed to get the network: ${e.javaClass.simpleName}: ${e.message}")
            throw error
        }

        try {
            contract = network.getContract(config.chaincodeName)
            ChaincodeApplication.logger.info { "Retrieving contract ${config.chaincodeName}" }
        } catch(e: Exception) {
            val error = ChaincodeTestrException("Failed to get the contract: ${e.javaClass.simpleName}: ${e.message}")
            throw error
        }

        ChaincodeApplication.logger.info { "Connection established to the HLF network and contract ${config.chaincodeName}" }
        return Connection(wallet, gateway, network, contract)
    }
}