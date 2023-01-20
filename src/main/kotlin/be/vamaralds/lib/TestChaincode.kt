package be.vamaralds.lib

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal

class TestChaincode: CliktCommand() {
    val walletPath by option(help = "Path to the wallet containing the identity to use for the connection").path()
    val identityName by option(help = "Name of the identity to use for the connection (default: Org1 Admin)").default("Org1 Admin")
    val connectionProfilePath by option(help = "Absolute Path to the connection profile").path()
    val chaincodeName by option(help = "Name of the chaincode to test")
    val testSuitePath by option(help = "Path to the testsuite to run").path()

    override fun run() {
        val config = ConnectionConfiguration(
            walletPath = walletPath!!.toAbsolutePath().toString(),
            identityName = identityName,
            connectionProfilePath = connectionProfilePath!!.toAbsolutePath().toString(),
            chaincodeName = chaincodeName!!
        )

        val terminal = Terminal()
        terminal.println("===== Configuring Chaincode Test Suite =====")
        terminal.println("Network and Gateway Configuration: $config")
        terminal.println("Test Suite Specification: ${testSuitePath!!.toAbsolutePath()}")

        terminal.println("===== Parsing Test Suite =====")
        var testSuite: ChaincodeTestSuite?
        try {
            testSuite = ChaincodeTestSuite.fromJson(testSuitePath!!.toFile().readText())
            terminal.println("Test Suite Successfully Parsed: ${testSuite.testsCases.size} test cases found")
        } catch(e: Exception) {
            terminal.println("Failed to parse the test suite: ${e.message}")
            return
        }

        terminal.println("===== Connection to the Network =====")
        var connection: Connection?
        try {
            connection = ChaincodeTestr(config).connect()
            terminal.println("Successfully connected to the network")
        } catch(e: Exception) {
            terminal.println("Failed to connect to the network: ${e.message}")
            return
        }

        terminal.println("===== Initializing Collaboration with Sender as Participants Handler =====")
        val contractHandler = ContractHandler(connection.contract)
        try {
            contractHandler.initializeCollaboration()
        } catch(e: ContractTransactionException) {
            terminal.println("Failed to initialize the collaboration: ${e.toString()}")
            return
        }

        terminal.println("===== Running Test Suite =====")
        val testSuiteResults = contractHandler.runTestSuite(testSuite)

        testSuiteResults.results.forEach { result ->
            when(val resVal = result.value) {
                is SuccessfulChaincodeTestResult -> terminal.println("Test Case: ${result.key.name} Successful ✅ ")
                is FailedChaincodeTestResult -> terminal.println("Test Case: ${result.key.name} Failed ❌: ${resVal.reasons.joinToString(",")}")
            }
        }

        val nbSuccess = testSuiteResults.results.count { it.value is SuccessfulChaincodeTestResult }
        val nbFailed = testSuiteResults.results.count { it.value is FailedChaincodeTestResult }
        terminal.println("===== Test Suite Results Summary =====")
        terminal.println("Number of Tests: ${testSuite.testsCases.size}")
        terminal.println("Number of Successful Tests: $nbSuccess")
        terminal.println("Number of Failed Tests: $nbFailed")

        terminal.println("===== Closing Gateway Connection =====")
        connection.gateway.close()
    }


}