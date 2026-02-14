import be.vamaralds.lib.*
import org.hyperledger.fabric.gateway.Contract
import java.util.logging.Logger
import java.util.logging.Level
import java.util.logging.LogManager

fun main(args: Array<String>) {
    // Keep application logs but suppress HLF verbose logs
    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn")
    System.setProperty("org.slf4j.simpleLogger.log.be.vamaralds", "info")
    System.setProperty("org.slf4j.simpleLogger.log.org.hyperledger.fabric.gateway", "error")
    System.setProperty("org.slf4j.simpleLogger.log.org.hyperledger.fabric.sdk", "error")
    System.setProperty("org.slf4j.simpleLogger.log.io.grpc", "error")
    System.setProperty("org.slf4j.simpleLogger.showDateTime", "false")
    System.setProperty("org.slf4j.simpleLogger.showThreadName", "false")
    System.setProperty("org.slf4j.simpleLogger.showLogName", "false")
    
    // Suppress OpenTelemetry span export warnings (java.util.logging)
    LogManager.getLogManager().getLogger("").level = Level.SEVERE
    Logger.getLogger("io.opentelemetry").level = Level.SEVERE
    Logger.getLogger("io.opentelemetry.sdk.internal").level = Level.SEVERE

    try {
        TestChaincode().main(args)
    } catch(e: Throwable) {
        ChaincodeApplication.logger.error { "${e.toString()}" }
    }
}