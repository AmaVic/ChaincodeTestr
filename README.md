# ChaincodeTestr
CLI application that supports the execution of a test suite on a B-MERODE chaincode, deployed on a HLF network.

# Running the Application
Using the terminal, go into the bin folder, and you can run the following commands:
```shell
./ChaincodeTestr --help 
```
You obtain the following output:
```shell
Usage: test-chaincode [OPTIONS]

Options:
  --wallet-path PATH              Path to the wallet containing the identity
                                  to use for the connection
  --identity-name TEXT            Name of the identity to use for the
                                  connection (default: Org1 Admin)
  --connection-profile-path PATH  Absolute Path to the connection profile
  --chaincode-name TEXT           Name of the chaincode to test
  --test-suite-path PATH          Path to the testsuite to run
  -h, --help                      Show this message and exit
```
To run a test suite, you need to specify all the options above. For instance:
```shell
./ChaincodeTestr --wallet-path chaincode-test/wallets/org1 --identity-name "Org1 Admin" --connection-profile-path chaincode-test/connection-profiles/org1.json --chaincode-name rea --test-suite-path chaincode-test/suites/happy-path-test-suite.json
```

Assuming that the HLF network is running and that the provided details are correct, the example provides the following output:
````shell
===== Configuring Chaincode Test Suite =====
Network and Gateway Configuration: {"identityName":"Org1 Admin","channelName":"mychannel","walletPath":"/Users/.../Downloads/ChaincoderTestr-1.0-SNAPSHOT/bin/chaincode-test/wallets/org1","connectionProfilePath":"/Users/.../Downloads/ChaincoderTestr-1.0-SNAPSHOT/bin/chaincode-test/connection-profiles/org1.json","chaincodeName":"rea"}
Test Suite Specification: /Users/.../Downloads/ChaincoderTestr-1.0-SNAPSHOT/bin/chaincode-test/suites/happy-path-test-suite.json
===== Parsing Test Suite =====
Test Suite Successfully Parsed: 8 test cases found
===== Connection to the Network =====
Successfully connected to the network
===== Initializing Collaboration with Sender as Participants Handler =====
===== Running Test Suite =====
Test Case: Create First EconomicAgent (Elmo) Successful ✅ 
Test Case: Create Second EconomicAgent(Cookie) Successful ✅ 
Test Case: Create Economic Resource Resource (Cookie) Successful ✅ 
Test Case: Create Economic Resource Resource (Cash) Successful ✅ 
Test Case: Register Ownership of Cash by Cookie Successful ✅ 
Test Case: Register Ownership of Cookie by Elmo Successful ✅ 
Test Case: Create Economic Event (delivery) Failed ❌: Expected transaction to succeed, but it failed (Transaction handleEvent failed: No valid proposal responses received. 2 peer error responses: Error during contract method execution; Error during contract method execution)
Test Case: Create Economic Event (payment) Failed ❌: Expected transaction to succeed, but it failed (Transaction handleEvent failed: No valid proposal responses received. 2 peer error responses: Error during contract method execution; Error during contract method execution)
===== Test Suite Results Summary =====
Number of Tests: 8
Number of Successful Tests: 6
Number of Failed Tests: 2
===== Closing Gateway Connection =====
````
