package be.vamaralds.lib

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal

class TestChaincode: CliktCommand() {
    // Legacy CLI parameters (v0.3)
    val legacy by option(help = "Use legacy v0.3 CLI interface").flag(default = false)
    val walletPath by option(help = "[LEGACY] Path to the wallet containing the identity to use for the connection").path()
    val identityName by option(help = "[LEGACY] Name of the identity to use for the connection").default("Org1 Admin")
    val connectionProfilePath by option(help = "[LEGACY] Absolute Path to the connection profile").path()
    
    // New CLI parameters (v1.0)
    val workspace by option(help = "Path to the easycc workspace root").path()
    
    // Common parameters
    val chaincodeName by option(help = "Name of the chaincode to test")
    val testSuitePath by option(help = "Path to the testsuite to run").path()

    override fun run() {
        if (legacy) {
            runLegacyMode()
        } else {
            runWorkspaceMode()
        }
    }

    private fun runLegacyMode() {
        @OptIn(com.github.ajalt.mordant.terminal.ExperimentalTerminalApi::class)
        val terminal = Terminal()
        terminal.println("===== Running in Legacy Mode (v0.3) =====")
        
        val config = ConnectionConfiguration(
            walletPath = walletPath!!.toAbsolutePath().toString(),
            identityName = identityName,
            connectionProfilePath = connectionProfilePath!!.toAbsolutePath().toString(),
            chaincodeName = chaincodeName!!
        )

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
        val nbSkipped = testSuite.testsCases.size - testSuiteResults.results.size
        
        terminal.println("===== Test Suite Results Summary =====")
        terminal.println("Number of Tests: ${testSuite.testsCases.size}")
        terminal.println("Number of Successful Tests: $nbSuccess")
        terminal.println("Number of Failed Tests: $nbFailed")
        if (nbSkipped > 0) {
            terminal.println("Number of Skipped Tests: $nbSkipped (stopped after first failure)")
        }

        terminal.println("===== Closing Gateway Connection =====")
        connection.gateway.close()
    }

    private fun runWorkspaceMode() {
        @OptIn(com.github.ajalt.mordant.terminal.ExperimentalTerminalApi::class)
        val terminal = Terminal()
        terminal.println("===== ChaincodeTestr v1.0 - Workspace Mode =====")
        
        // Validate required parameters
        if (workspace == null || testSuitePath == null || chaincodeName == null) {
            terminal.println("Error: Missing required parameters")
            terminal.println("Required: --workspace, --test-suite-path, --chaincode-name")
            return
        }
        
        terminal.println("Workspace: ${workspace!!.toAbsolutePath()}")
        terminal.println("Test Suite: ${testSuitePath!!.toAbsolutePath()}")
        terminal.println("Chaincode: $chaincodeName")
        
        // Initialize workspace manager
        terminal.println("===== Initializing Workspace =====")
        var workspaceManager: WorkspaceManager?
        try {
            workspaceManager = WorkspaceManager(workspace!!.toAbsolutePath().toString())
            val orgs = workspaceManager.getOrganizations()
            terminal.println("Discovered ${orgs.size} organization(s): ${orgs.joinToString(", ")}")
        } catch(e: WorkspaceException) {
            terminal.println("Failed to initialize workspace: ${e.message}")
            return
        }
        
        // Parse test suite
        terminal.println("===== Parsing Test Suite =====")
        var testSuite: ChaincodeTestSuite?
        try {
            testSuite = ChaincodeTestSuite.fromJson(testSuitePath!!.toFile().readText())
            terminal.println("Test Suite Successfully Parsed: ${testSuite.testsCases.size} test cases found")
        } catch(e: Exception) {
            terminal.println("Failed to parse the test suite: ${e.message}")
            return
        }
        
        // Find initialization organization (test case with thenMarkAsReady: true)
        val initTestCase = testSuite.testsCases.find { it.thenMarkAsReady }
        if (initTestCase == null) {
            terminal.println("Error: No test case found with 'thenMarkAsReady: true'. Cannot determine initialization organization.")
            return
        }
        
        val initOrg = initTestCase.submittedBy
        if (initOrg == null) {
            terminal.println("Error: Initialization test case '${initTestCase.name}' must have 'submittedBy' field specified.")
            return
        }
        
        terminal.println("Initialization Organization: $initOrg (from test case: '${initTestCase.name}')")
        
        // Initialize multi-org connection manager
        terminal.println("===== Initializing Connection Manager =====")
        val connectionManager = MultiOrgConnectionManager(workspaceManager, chaincodeName!!)
        val payloadResolver = PayloadResolver(workspaceManager)
        
        try {
            // Get initial connection
            val initConnection = connectionManager.getConnection(initOrg)
            terminal.println("Successfully connected to network as $initOrg")
            
            // Initialize collaboration
            terminal.println("===== Initializing Collaboration as $initOrg =====")
            val initContractHandler = ContractHandler(initConnection.contract)
            try {
                initContractHandler.initializeCollaboration()
                terminal.println("Collaboration initialized successfully")
            } catch(e: ContractTransactionException) {
                terminal.println("Failed to initialize the collaboration: ${e.message}")
                connectionManager.closeAll()
                return
            }
            
            // Run test suite
            terminal.println("\n========================================")
            terminal.println("  Running Test Suite")
            terminal.println("========================================")
            val testSuiteResults = runMultiOrgTestSuite(
                testSuite, 
                connectionManager, 
                payloadResolver,
                initOrg,
                initContractHandler,
                terminal
            )
            
            // Display summary
            val nbSuccess = testSuiteResults.results.count { it.value is SuccessfulChaincodeTestResult }
            val nbFailed = testSuiteResults.results.count { it.value is FailedChaincodeTestResult }
            val nbSkipped = testSuite.testsCases.size - testSuiteResults.results.size
            
            terminal.println("\n========================================")
            terminal.println("  SUMMARY")
            terminal.println("========================================")
            terminal.println("Total Tests:  ${testSuite.testsCases.size}")
            terminal.println("Passed:       $nbSuccess ✅")
            terminal.println("Failed:       $nbFailed ❌")
            if (nbSkipped > 0) {
                terminal.println("Skipped:      $nbSkipped ⏭️  (stopped after failure)")
            }
            terminal.println("========================================")
            
            terminal.println("===== Closing Connections =====")
            connectionManager.closeAll()
            
        } catch(e: Exception) {
            terminal.println("Error during test execution: ${e.message}")
            connectionManager.closeAll()
        }
    }
    
    private fun runMultiOrgTestSuite(
        testSuite: ChaincodeTestSuite,
        connectionManager: MultiOrgConnectionManager,
        payloadResolver: PayloadResolver,
        defaultOrg: String,
        validationHandler: ContractHandler,
        terminal: Terminal
    ): ChaincodeTestSuiteResult {
        val results = mutableMapOf<ChaincodeTestCase, ChaincodeTestResult>()
        
        for ((index, testCase) in testSuite.testsCases.withIndex()) {
            val orgName = testCase.submittedBy ?: defaultOrg
            val testNumber = index + 1
            val totalTests = testSuite.testsCases.size
            
            terminal.println("\n[Test $testNumber/$totalTests]")
            terminal.println("  Name: ${testCase.name}")
            terminal.println("  Org:  $orgName")
            terminal.print("  Status: Running...")
            
            val connection = connectionManager.getConnection(orgName)
            val contractHandler = ContractHandler(connection.contract)
            
            // Resolve payload placeholders
            val resolvedPayload = payloadResolver.resolvePayload(testCase.payload)
            val resolvedTestCase = testCase.copy(payload = resolvedPayload)
            
            // Submit transaction from the specified org, but validate from init org's perspective
            val result = contractHandler.runSingleTest(resolvedTestCase, validationHandler)
            results[testCase] = result
            
            // Show result immediately
            when(val resVal = result) {
                is SuccessfulChaincodeTestResult -> {
                    terminal.println("\r  Status: ✅ PASSED")
                    
                    // Display created/modified objects
                    if (resVal.validatedObjects.isNotEmpty()) {
                        val primaryObject = resVal.validatedObjects.entries.firstOrNull()
                        primaryObject?.let {
                            terminal.println("  Result: Created/Modified ${it.key}")
                            val name = try { it.value.getAttributeValue<Any>("name") } catch(e: Exception) { null }
                            if (name != null) {
                                terminal.println("          └─ name: $name")
                            }
                        }
                    }
                }
                is FailedChaincodeTestResult -> {
                    terminal.println("\r  Status: ❌ FAILED")
                    terminal.println("  Error:  ${resVal.reasons.joinToString(", ")}")
                    terminal.println("\n⚠️  Test failed - stopping test suite execution")
                    break  // Stop on first failure
                }
            }
        }
        
        return ChaincodeTestSuiteResult(testSuite.name, results)
    }
}

