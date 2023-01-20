# ChaincodeTestr
[![Build](https://github.com/AmaVic/ChaincodeTestr/actions/workflows/build.yml/badge.svg)](https://github.com/AmaVic/ChaincodeTestr/actions/workflows/build.yml)


CLI application that supports the execution of a test suite on a B-MERODE chaincode, deployed on a HLF network.
The below video provides the explanation and a demonstration. The remainder of the README provides all the detailed explanations.

[![B-MERODE Chaincode Tester Demo](https://img.youtube.com/vi/V4gcxtX9kZk/0.jpg)](https://www.youtube.com/watch?v=V4gcxtX9kZk)


# Running the Application
Using the terminal, go into the bin folder, and you can run the following commands:
```shell
./ChaincoderTestr --help 
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
./ChaincoderTestr --wallet-path chaincode-test/wallets/org1 --identity-name "Org1 Admin" --connection-profile-path chaincode-test/connection-profiles/org1.json --chaincode-name rea --test-suite-path chaincode-test/suites/happy-path-test-suite.json
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
_Note: additional logging output is generated by the HLF gateway client, they can be ignored._

# Extracting the Wallets and Connection Profiles
To obtain the wallet folder and the connection profile file, you can use the export functionality of the IBM Blockchain Platform extension for VSCode.

# Preparing the Test Suite Specification
To specify the test suites, the application relies on a _.json_.

The fields to specify are the following:
* _name_: the name of the test suite
* _testsCases_: an array of test cases to run

Each test case is specified as follows:
* _name_: the name of the test case
* _businessEventName_: the name of the business event to fire
* _payload_: the payload of the business event (JSON)
* _expectedToSucceed_: true / false: defines whether the business event is expected to be successfully executed or not
* _thenMarkAsReady_: true / false: defines whether the B-MERODE collaboration shuold be marked as ready after the execution of the business event
* _expectedAttributeValues_: an array of tests to run on the attributes of the business event. Each test is specified as follows:
  * _boId_: id of the business object whose attributes values will be tested
  * _attributeName_: the name of the attribute (_starting with a lower-case, even if the business object attribute name in the chaincode starts with an upper-case_)
  * _expectedValue_: expected value for the attribute
* _expectedBOStates_: an array of tests to run on the states of the business objects. Each test is specified as follows:
  * _boId_: id of the business object whose state will be tested
  * _expectedStateName_: expected state (name) for the business object

Note that the _expectedAttributeValues_ and the _expectedBOStates_ are optional. If not specified, the application will not perform any test on the attributes and the states of the business objects.
Also, these fields allow specifying tests for any business object (not only the one created/modified after the execution of the business event), as long as the _boId_ is correctly specified. It is therefore possible to check the propagated effects of business events.

````json
{
    "name": "Happy Path REA",
    "testsCases": [
        {
            "name": "Create First EconomicAgent (Elmo)",
            "businessEventName": "EVcrEconomicAgent",
            "payload": {
                "publicKey": "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEOqHVWeIlraubwG+CsmobuigvO0TeaXJbmS8QkkwTOTtBtogKFVjj9PyBt0hekRKy8wtVllQpfu+UgfrITok5BA==",
                "Name": "Elmo"
            },
            "expectedToSucceed": true,
            "thenMarkAsReady": true,
            "expectedAttributeValues": [
                {
                    "boId": "EconomicAgent#0",
                    "attributeName": "name",
                    "expectedValue": "Elmo"
                }
            ],
            "expectedBOStates": [
                {
                    "boId": "EconomicAgent#0",
                    "expectedStateName": "exists"
                }
            ]
        },
        {
            "name": "Create Second EconomicAgent(Cookie)",
            "businessEventName": "EVcrEconomicAgent",
            "payload": {
                "publicKey": "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEMkHRF1L6deb2FCwQkZbwNeGtp00kc/sxd2R5XLfiGCM6yA9F4NJXPg1DdNZ45x2YqcDn5lBJKkzHvNMVADMBIw==",
                "Name": "Cookie"
            },
            "expectedToSucceed": true,
            "thenMarkAsReady": false,
            "expectedAttributeValues": [
                {
                    "boId": "EconomicAgent#1",
                    "attributeName": "name",
                    "expectedValue": "Cookie"
                }
            ],
            "expectedBOStates": [
                {
                    "boId": "EconomicAgent#0",
                    "expectedStateName": "exists"
                },
                {
                    "boId": "EconomicAgent#1",
                    "expectedStateName": "exists"
                }
            ]
        },
        {
            "name": "Create Economic Resource Resource (Cookie)",
            "businessEventName": "EVcrEconomicResource",
            "payload": {
                "Name": "Cookie"
            },
            "expectedToSucceed": true,
            "thenMarkAsReady": false,
            "expectedAttributeValues": [
                {
                    "boId": "EconomicResource#0",
                    "attributeName": "name",
                    "expectedValue": "Cookie"
                }
            ],
            "expectedBOStates": [
                {
                    "boId": "EconomicAgent#0",
                    "expectedStateName": "exists"
                },
                {
                    "boId": "EconomicAgent#1",
                    "expectedStateName": "exists"
                },
                {
                    "boId": "EconomicResource#0",
                    "expectedStateName": "exists"
                }
            ]
        },
        {
            "name": "Create Economic Resource Resource (Cash)",
            "businessEventName": "EVcrEconomicResource",
            "payload": {
                "Name": "Cash"
            },
            "expectedToSucceed": true,
            "thenMarkAsReady": false,
            "expectedAttributeValues": [
                {
                    "boId": "EconomicResource#1",
                    "attributeName": "name",
                    "expectedValue": "Cash"
                }
            ],
            "expectedBOStates": [
                {
                    "boId": "EconomicAgent#0",
                    "expectedStateName": "exists"
                },
                {
                    "boId": "EconomicAgent#1",
                    "expectedStateName": "exists"
                },
                {
                    "boId": "EconomicResource#0",
                    "expectedStateName": "exists"
                },
                {
                    "boId": "EconomicResource#1",
                    "expectedStateName": "exists"
                }
            ]
        },
        {
            "name": "Register Ownership of Cash by Cookie",
            "businessEventName": "EVcrOwnership",
            "payload": {
                "Name": "CashOwnedByCookie",
                "economicResourceId_EconomicResource_Ownership": "EconomicResource#1",
                "economicAgentId_EconomicAgent_Ownership": "EconomicAgent#1"
            },
            "expectedToSucceed": true,
            "thenMarkAsReady": false,
            "expectedAttributeValues": [
                {
                    "boId": "Ownership#0",
                    "attributeName": "name",
                    "expectedValue": "CashOwnedByCookie"
                }
            ],
            "expectedBOStates": [
                {
                    "boId": "EconomicAgent#0",
                    "expectedStateName": "exists"
                },
                {
                    "boId": "EconomicAgent#1",
                    "expectedStateName": "exists"
                },
                {
                    "boId": "EconomicResource#0",
                    "expectedStateName": "exists"
                },
                {
                    "boId": "EconomicResource#1",
                    "expectedStateName": "exists"
                },
                {
                    "boId": "Ownership#0",
                    "expectedStateName": "image"
                }
            ]
        },
        {
            "name": "Register Ownership of Cookie by Elmo",
            "businessEventName": "EVcrOwnership",
            "payload": {
                "Name": "CookieOwnedByElmo",
                "economicResourceId_EconomicResource_Ownership": "EconomicResource#0",
                "economicAgentId_EconomicAgent_Ownership": "EconomicAgent#0"
            },
            "expectedToSucceed": true,
            "thenMarkAsReady": false,
            "expectedAttributeValues": [
                {
                    "boId": "Ownership#1",
                    "attributeName": "name",
                    "expectedValue": "CookieOwnedByElmo"
                }
            ],
            "expectedBOStates": [
                {
                    "boId": "EconomicAgent#0",
                    "expectedStateName": "exists"
                },
                {
                    "boId": "EconomicAgent#1",
                    "expectedStateName": "exists"
                },
                {
                    "boId": "EconomicResource#0",
                    "expectedStateName": "exists"
                },
                {
                    "boId": "EconomicResource#1",
                    "expectedStateName": "exists"
                },
                {
                    "boId": "Ownership#0",
                    "expectedStateName": "image"
                },
                {
                    "boId": "Ownership#1",
                    "expectedStateName": "image"
                }
            ]
        },
        {
            "name": "Create Economic Event (delivery)",
            "businessEventName": "EVcrEconomicEvent",
            "payload": {
                "Name": "delivery",
                "TimeStamp": null,
                "CountDependentViews": 0
            },
            "expectedToSucceed": true,
            "thenMarkAsReady": false,
            "expectedAttributeValues": [
                {
                    "boId": "EconomicEvent#0",
                    "attributeName": "name",
                    "expectedValue": "delivery"
                }
            ],
            "expectedBOStates": [
                {
                    "boId": "EconomicAgent#0",
                    "expectedStateName": "exists"
                },
                {
                    "boId": "EconomicAgent#1",
                    "expectedStateName": "exists"
                },
                {
                    "boId": "EconomicResource#0",
                    "expectedStateName": "exists"
                },
                {
                    "boId": "EconomicResource#1",
                    "expectedStateName": "exists"
                },
                {
                    "boId": "Ownership#0",
                    "expectedStateName": "image"
                },
                {
                    "boId": "Ownership#1",
                    "expectedStateName": "image"
                },
                {
                    "boId": "EconomicEvent#0",
                    "expectedStateName": "business event"
                }
            ]
        },
        {
            "name": "Create Economic Event (payment)",
            "businessEventName": "EVcrEconomicEvent",
            "payload": {
                "Name": "payment",
                "TimeStamp": null,
                "CountDependentViews": 0
            },
            "expectedToSucceed": true,
            "thenMarkAsReady": false,
            "expectedAttributeValues": [
                {
                    "boId": "EconomicEvent#1",
                    "attributeName": "name",
                    "expectedValue": "payment"
                }
            ],
            "expectedBOStates": [
                {
                    "boId": "EconomicAgent#0",
                    "expectedStateName": "exists"
                },
                {
                    "boId": "EconomicAgent#1",
                    "expectedStateName": "exists"
                },
                {
                    "boId": "EconomicResource#0",
                    "expectedStateName": "exists"
                },
                {
                    "boId": "EconomicResource#1",
                    "expectedStateName": "exists"
                },
                {
                    "boId": "Ownership#0",
                    "expectedStateName": "image"
                },
                {
                    "boId": "Ownership#1",
                    "expectedStateName": "image"
                },
                {
                    "boId": "EconomicEvent#0",
                    "expectedStateName": "business event"
                },
                {
                    "boId": "EconomicEvent#1",
                    "expectedStateName": "business event"
                }
            ]
        }
    ]
}
````

When preparing the test suite specification, do not forger to set the public keys of the participants that are created, and to update them everytime a new network is created.
Indeed, without at least one valid particpant, once the collaboration is initialized, all business events invocation will fail.
The public keys can be retrieved by invoking the _getSenderPk_ transaction on the chaincode, using the different wallets that are available, in the IBM Platform Blockchain VSCode extension.

When expected attribute values or expected states tests fail, an indication will be displayed in the test results.
Note that this is a _preliminary_ version of the software and that not all errors are handled properly. When errors occur, it might therefore be useful to read the HLF network's logs to have further details on the errors that occurred.

# Setting up the Test Suite
To ensure that a test suite can be executed correctly, you need, for every run of the suite, to:
1. Create a new HLF network
2. Deploy the chaincode on the network
3. Retrieve and export the wallets and connection profiles to use to the appropriate files
4. Retrieve the public key(s) of the participants to create

The program will then be executed as follows:
1. Connect to the HLF network and retrieve the chaincode
2. Initialize the B-MERODE collaboration using the sender of the transaction as participants handler
3. Execute each test in the test suite, even if some of them fail, and output the results
4. Output the test suite results summary
5. Close the connection to the HLF network
