package be.vamaralds.lib

import org.hyperledger.fabric.gateway.Contract
import org.hyperledger.fabric.gateway.Gateway
import org.hyperledger.fabric.gateway.Network
import org.hyperledger.fabric.gateway.Wallet

data class Connection(
    val wallet: Wallet,
    val gateway: Gateway,
    val network: Network,
    val contract: Contract
)