import be.vamaralds.lib.*
import org.hyperledger.fabric.gateway.Contract
import java.util.logging.Logger

fun main(args: Array<String>) {
    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "off");

    try {
        TestChaincode().main(args)
    } catch(e: Throwable) {
        ChaincodeApplication.logger.error { "${e.toString()}" }
    }
}