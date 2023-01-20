package be.vamaralds.lib

import org.hyperledger.fabric.gateway.Contract
import org.hyperledger.fabric.gateway.ContractException
import org.hyperledger.fabric.sdk.exception.TransactionException
import org.json.JSONObject
import java.util.concurrent.TimeoutException
import kotlin.jvm.Throws

class ContractTransactionException(message: String) : Exception(message)

class ContractHandler(private val contract: Contract) {

    fun runTestSuite(testSuite: ChaincodeTestSuite): ChaincodeTestSuiteResult {
        val results = testSuite.testsCases.map { testCase ->
            Pair(testCase, runTest(testCase))
        }.toMap()

        return ChaincodeTestSuiteResult(testSuite.name, results)
    }

    private fun runTest(test: ChaincodeTestCase): ChaincodeTestResult {
        ChaincodeApplication.logger.info { "Running test: ${test.name}" }
        val errors = mutableListOf<String>()
        try {
            handleEvent(test.businessEventName, test.payload)
            if(!test.expectedToSucceed)
                return FailedChaincodeTestResult(test, listOf("Expected transaction to fail, but it succeeded"))

            if(test.thenMarkAsReady)
                markCollaborationAsReady()

        } catch(e: ContractTransactionException) {
            if(test.expectedToSucceed)
                return FailedChaincodeTestResult(test, listOf("Expected transaction to succeed, but it failed (${e.message})"))
        }

        test.expectedAttributeValues.forEach { expectedAttributeValue ->
            var boJson: JsonBusinessObject?
            try {
                boJson = getBusinessObject(expectedAttributeValue.boId)
            } catch(e: ContractTransactionException) {
                errors.add("Failed to get Business Object with id: ${expectedAttributeValue.boId}")
                return FailedChaincodeTestResult(test, errors)
            }

            val actualValue = boJson.getAttributeValue<Any>(expectedAttributeValue.attributeName)
            if(actualValue != expectedAttributeValue.expectedValue) {
                errors.add("Expected value for attribute ${expectedAttributeValue.attributeName} of Business Object ${expectedAttributeValue.boId} to be ${expectedAttributeValue.expectedValue}, but it was $actualValue")
            }
        }

        test.expectedBOStates.forEach { expectedState ->
            var boJson: JsonBusinessObject?
            try {
                boJson = getBusinessObject(expectedState.boId)
            } catch(e: ContractTransactionException) {
                errors.add("Failed to get Business Object with id: ${expectedState.boId}")
                return FailedChaincodeTestResult(test, errors)
            }

            val actualStateName = boJson!!.getState()
            if(actualStateName != expectedState.expectedStateName) {
                errors.add("Expected state of Business Object ${expectedState.boId} to be ${expectedState.expectedStateName}, but it was $actualStateName")
            }
        }

        return if(errors.isEmpty())
            SuccessfulChaincodeTestResult(test)
        else
            FailedChaincodeTestResult(test, errors)
    }

    @Throws(ContractTransactionException::class)
    fun markCollaborationAsReady() {
        try {
            contract.submitTransaction("markCollaborationAsReady")
        } catch (e: ContractException) {
            throw ContractTransactionException(e.message ?: "Unknown error")
        } catch (e: TimeoutException) {
            throw ContractTransactionException(e.message ?: "Unknown error")
        } catch (e: TransactionException) {
            throw ContractTransactionException(e.message ?: "Unknown error")
        }
    }


    @Throws(ContractTransactionException::class)
    fun initializeCollaboration(senderPk: String? = null) {
        if(evaluateTransaction("isInitialized").toBoolean()) {
            return
        }

        if(senderPk == null) {
            contract.submitTransaction("initWithSenderAsParticipantsHandler")
        } else {
            val payload = JSONObject(mapOf(
                "senderPk" to senderPk
            )).toString()

            contract.submitTransaction("init", payload)
        }
    }

    @Throws(ContractTransactionException::class)
    fun handleEvent(event: String, payload: String): String {
        //ChaincodeApplication.logger.info { "Submitting Business Event: $event with payload: $payload" }
        return submitTransaction("handleEvent", event, payload)
    }

    @Throws(ContractTransactionException::class)
    fun handleEvent(event: String, payload: Map<String, Any>): String =
        handleEvent(event, JSONObject(payload).toString())

    @Throws(ContractTransactionException::class)
    fun getBusinessObject(id: String): JsonBusinessObject =
        JsonBusinessObject(evaluateTransaction("getBusinessObject", id))

    @Throws(ContractTransactionException::class)
    fun submitTransaction(transactionName: String, vararg args: String): String {
        ChaincodeApplication.logger.info { "Submitting transaction $transactionName with args: ${args.joinToString()}" }
        try {
            val result = contract.submitTransaction(transactionName, *args).toString(Charsets.UTF_8)
            ChaincodeApplication.logger.info { "Transaction $transactionName submitted successfully - Result: $result" }
            return result
        } catch(e: Exception) {
            when(e) {
                is TimeoutException -> {
                    val error = ContractTransactionException("Transaction $transactionName timed out")
                    ChaincodeApplication.logger.error(error) { "Submitting transaction $transactionName: Failed: $error" }
                    throw error
                }
                is ContractException -> {
                    val error = ContractTransactionException("Transaction $transactionName failed: ${e.message}")
                    ChaincodeApplication.logger.error(error) { "Submitting transaction $transactionName: Failed: $error" }
                    throw error
                }
                is TransactionException -> {
                    val error = ContractTransactionException("Transaction $transactionName failed: ${e.message}")
                    ChaincodeApplication.logger.error(error) { "Submitting transaction $transactionName: Failed: $error" }
                    throw error
                }
                else -> {
                    val error = TransactionException("Transaction $transactionName failed: ${e.message}")
                    ChaincodeApplication.logger.error(error) { "Submitting transaction $transactionName: Failed: $error" }
                    throw error
                }
            }
        }
    }

    @Throws(ContractTransactionException::class)
    fun evaluateTransaction(transactionName: String, vararg args: String): String {
        ChaincodeApplication.logger.info { "Evaluating transaction $transactionName with args: ${args.joinToString()}" }
        try {
            val result = contract.evaluateTransaction(transactionName, *args).toString(Charsets.UTF_8)
            ChaincodeApplication.logger.info { "Transaction $transactionName evaluated successfully - Result: $result" }
            return result
        } catch(e: Exception) {
            when(e) {
                is TimeoutException -> {
                    val error = ContractTransactionException("Transaction $transactionName timed out")
                    //ChaincodeApplication.logger.error(error) { "Evaluating transaction $transactionName: Failed: $error" }
                    throw error
                }
                is ContractException -> {
                    val error = ContractTransactionException("Transaction $transactionName failed: ${e.message}")
                    //ChaincodeApplication.logger.error(error) { "Evaluating transaction $transactionName: Failed: $error" }
                    throw error
                }
                is TransactionException -> {
                    val error = ContractTransactionException("Transaction $transactionName failed: ${e.message}")
                    //ChaincodeApplication.logger.error(error) { "Evaluating transaction $transactionName: Failed: $error" }
                    throw error
                }
                else -> {
                    val error = TransactionException("Transaction $transactionName failed: ${e.message}")
                    //ChaincodeApplication.logger.error(error) { "Evaluating transaction $transactionName: Failed: $error" }
                    throw error
                }
            }
        }
    }
}