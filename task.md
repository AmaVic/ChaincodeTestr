The software we have here is designed to run test suites on chaincodes running on Hyperledger network.

Check out the README to see how to use it, and how "test suites" are specified. You can also find examples of test suites there: ./src/test/resources/happy-path-test-suite.json

When you want to run a test suite on a deploy chaincode on a running network, we use the following command: 
./ChaincoderTestr --wallet-path chaincode-test/wallets/org1 --identity-name "Org1 Admin" --connection-profile-path chaincode-test/connection-profiles/org1.json --chaincode-name chaincode --test-suite-path 
...

Currently, the software has atwo limitation I want to lift. It is that you have to specify the wallet/connection-profile pair you want to use to execute transactions; and that in the test suite files, when a public key has to be specified, you have to provide it manually.

In the new setup that I want, a user should be able to specicfy, directly in the test suite, as which organization it wants to submit a transaction, by specifying the "name" or the organization. The software should then fetch the details of the corresponding wallet/connection profile to submit the transaction. Also, when referring to public keys in the testsuite, again, specifying the name of the organization instead would be much easier and repetable.

The new version will work alongside another software, called easycc. It supports the creation and deployment of HLF networks and chaincodes, and exports credentials for multiple organizations. You can find an example of folder that contains a workspace of easycc, with details on the credentials/wallet, etc. in the directories, and which includes multiple organizations. If I create a network with two organizations (say "supplier" and "provider"), I should be able to say, for each transaction in the test suite, whether I want to run it as supplier or provider, and be able to specify the "supplier's public key" by mentioning "supplier" instead of the actual public key (same for all types of participants). The type of participant / name of organization is not pre-defined but is user-defined when the network is created and the test suite is written.

Here's an example of how to run the program current version: ./ChaincoderTestr --wallet-path /Users/vamarald/Dev/testn/exports/wallets/org1 --identity-name "Org1" --connection-profile-path /Users/vamarald/Dev/testn/exports/connection-profiles/connection-org1.json --chaincode-name chaincode --test-suite-path /Users/vamarald/Dev/ChaincodeTestr/src/test/resources/happy-path-test-suite.json

Here's how I want to be able to run the new version, with the other changes above:
./Chaincodetestr --worskspace ROOT_OF_EASYCC_WORKSPACE
--test-suite-path PATH_TO_TEST_SUITE
--init-as NAME_OF_INITIALIZING_ORGANIZATION
--chaincode-name CC_NAME
--chaincode-version VERSION

You can look in-depth in:
~/Dev/testn: to see the content of a easycc sample workspace
~/Dev/easycc: to see the full source code of easycc, to know how it maps organization names to outputs in different ways (wallets, connection profiles, etc.).

Also, keep a markdown file with the original README (for version 0.3), but create a new, update README.md that will include updated documentation, details on the changes from v0.3 to v1.0 (the version we are going to implement), and a link to the older version's readme.

Before you make any changes, make a thorough review of the concerned softwares, and propose me a comprehensive plan for the implementation of the changes that are required. If you have any question or clarification required, ask it now if possible, but later questions are of course allowed.
