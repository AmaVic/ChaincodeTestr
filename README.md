# ChaincodeTestr v1.0
[![Build](https://github.com/AmaVic/ChaincodeTestr/actions/workflows/build.yml/badge.svg)](https://github.com/AmaVic/ChaincodeTestr/actions/workflows/build.yml)

CLI application that supports the execution of test suites on B-MERODE chaincodes deployed on Hyperledger Fabric networks, with integrated multi-organization support for [**easycc**](https://github.com/AmaVic/easycc) workspaces.

## What's New in v1.0

Version 1.0 introduces **multi-organization support** designed to work seamlessly with [**easycc**](https://github.com/AmaVic/easycc) workspaces:

- **Multi-Organization Testing**: Execute transactions from different organizations within a single test suite
- **Automatic Credential Discovery**: No need to manually specify wallets and connection profiles
- **Organization Placeholders**: Reference organization public keys using simple placeholders (`$org1`, `$org2`, etc.)
- **Simplified CLI**: Just point to your easycc workspace and test suite
- **Backward Compatible**: Legacy v0.3 mode still available with `--legacy` flag

For v0.3 documentation, see [README-v0.3.md](README-v0.3.md).

## Quick Start

### Installation

Build the application using Gradle:
```bash
./gradlew build
./gradlew installDist
```

The executable will be available at: `build/install/ChaincoderTestr/bin/ChaincoderTestr`

### Running a Test Suite (v1.0)

```bash
./ChaincoderTestr --workspace /path/to/easycc/workspace \
                  --test-suite-path test-suite.json \
                  --chaincode-name myChaincode
```

### Legacy Mode (v0.3)

For backward compatibility with v0.3:
```bash
./ChaincoderTestr --legacy \
                  --wallet-path path/to/wallet \
                  --identity-name "Org1 Admin" \
                  --connection-profile-path path/to/profile.json \
                  --chaincode-name myChaincode \
                  --test-suite-path test-suite.json
```

## Usage

### Command-Line Options

#### v1.0 Mode (Default)
```
Options:
  --workspace PATH          Path to the easycc workspace root directory
  --test-suite-path PATH    Path to the test suite JSON file
  --chaincode-name TEXT     Name of the chaincode to test
  -h, --help               Show this message and exit
```

#### Legacy Mode (v0.3)
```
Options:
  --legacy                        Enable legacy v0.3 mode
  --wallet-path PATH              Path to the wallet directory
  --identity-name TEXT            Identity name (default: "Org1 Admin")
  --connection-profile-path PATH  Path to connection profile JSON
  --chaincode-name TEXT           Name of the chaincode to test
  --test-suite-path PATH          Path to the test suite JSON file
  -h, --help                      Show this message and exit
```

## Test Suite Specification

### v1.0 Format

Test suites are defined in JSON format with organization-aware test cases:

```json
{
    "name": "Multi-Organization Test Suite",
    "testsCases": [
        {
            "name": "Create First Agent as Org1",
            "submittedBy": "org1",
            "businessEventName": "EVcrEconomicAgent",
            "payload": {
                "publicKey": "$org1",
                "Name": "Agent 1"
            },
            "expectedToSucceed": true,
            "thenMarkAsReady": true,
            "expectedAttributeValues": [
                {
                    "boId": "EconomicAgent#0",
                    "attributeName": "name",
                    "expectedValue": "Agent 1"
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
            "name": "Create Second Agent as Org2",
            "submittedBy": "org2",
            "businessEventName": "EVcrEconomicAgent",
            "payload": {
                "publicKey": "$org2",
                "Name": "Agent 2"
            },
            "expectedToSucceed": true,
            "thenMarkAsReady": false,
            "expectedAttributeValues": [ ... ],
            "expectedBOStates": [ ... ]
        }
    ]
}
```

### Test Case Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | Yes | Name of the test case |
| `submittedBy` | string | No | Organization name that submits this transaction (defaults to initialization org) |
| `businessEventName` | string | Yes | Name of the business event to invoke |
| `payload` | object | Yes | JSON payload for the transaction (supports `$orgName` placeholders) |
| `expectedToSucceed` | boolean | Yes | Whether the transaction is expected to succeed |
| `thenMarkAsReady` | boolean | Yes | Whether to mark collaboration as ready after this transaction |
| `expectedAttributeValues` | array | No | Expected attribute values to verify after transaction |
| `expectedBOStates` | array | No | Expected business object states to verify after transaction |

### Key Concepts

#### 1. Organization Placeholders

Use `$orgName` syntax in your payload to automatically resolve to the organization's public key:

```json
{
    "payload": {
        "publicKey": "$org1",
        "Name": "My Agent"
    }
}
```

This will be automatically replaced with the actual public key from `workspace/exports/wallets/org1/public_key.txt`.

#### 2. Initialization Organization

The initialization organization is automatically determined from the test case with `"thenMarkAsReady": true`. The `submittedBy` field of that test case specifies which organization initializes the collaboration.

```json
{
    "name": "Initialize Collaboration",
    "submittedBy": "org1",
    "thenMarkAsReady": true,
    ...
}
```

#### 3. Transaction Submission

Each test case can specify a different organization via `submittedBy`. If omitted, it defaults to the initialization organization.

## EasyCC Workspace Structure

ChaincodeTestr v1.0 is designed to work with workspaces generated by [**easycc**](https://github.com/AmaVic/easycc), a tool for easily deploying and managing Hyperledger Fabric networks with multiple organizations.

The tool expects the following structure in your easycc workspace:

```
workspace/
└── exports/
    ├── wallets/
    │   ├── org1/
    │   │   ├── admin.id            # Admin identity credentials
    │   │   ├── public_key.txt      # Public key (base64)
    │   │   └── metadata.json       # Organization metadata
    │   └── org2/
    │       └── ... (same structure)
    └── connection-profiles/
        ├── connection-org1.json    # Connection profile for org1
        └── connection-org2.json    # Connection profile for org2
```

The application automatically:
- Discovers all organizations in `exports/wallets/`
- Loads connection profiles from `exports/connection-profiles/connection-{orgName}.json`
- Uses the `admin` identity for all organizations
- Reads public keys from `public_key.txt` files

## Example Output

```
===== ChaincodeTestr v1.0 - Workspace Mode =====
Workspace: /Users/vamarald/Dev/testn
Test Suite: /Users/vamarald/Dev/ChaincodeTestr/src/test/resources/multi-org-test-suite.json
Chaincode: mychaincode
===== Initializing Workspace =====
Discovered 2 organization(s): org1, org2
===== Parsing Test Suite =====
Test Suite Successfully Parsed: 8 test cases found
Initialization Organization: org1 (from test case: 'Create First EconomicAgent (Elmo) as Org1')
===== Initializing Connection Manager =====
Successfully connected to network as org1
===== Initializing Collaboration as org1 =====
Collaboration initialized successfully

========================================
  Running Test Suite
========================================

[Test 1/8]
  Name: Create First EconomicAgent (Elmo) as Org1
  Org:  org1
  Status: ✅ PASSED.
  Result: Created/Modified EconomicAgent#0
          └─ name: Elmo

[Test 2/8]
  Name: Create Second EconomicAgent (Cookie) as Org1 for Org2
  Org:  org1
  Status: ✅ PASSED.
  Result: Created/Modified EconomicAgent#1
          └─ name: Cookie

[Test 3/8]
  Name: Create Economic Resource (Cookie) from Org1
  Org:  org1
  Status: ✅ PASSED.
  Result: Created/Modified EconomicResource#0
          └─ name: Cookie

[Test 4/8]
  Name: Create Economic Resource (Cash) from Org2
  Org:  org2
  Status: ✅ PASSED.
  Result: Created/Modified EconomicResource#1
          └─ name: Cash

[Test 5/8]
  Name: Register Ownership of Cash by Cookie (from Org2)
  Org:  org2
  Status: ✅ PASSED.
  Result: Created/Modified Ownership#0
          └─ name: CashOwnedByCookie

[Test 6/8]
  Name: Register Ownership of Cookie by Elmo (from Org1)
  Org:  org1
  Status: ✅ PASSED.
  Result: Created/Modified Ownership#1
          └─ name: CookieOwnedByElmo

[Test 7/8]
  Name: Create Economic Event (delivery) from Org1
  Org:  org1
  Status: ✅ PASSED.
  Result: Created/Modified EconomicEvent#0
          └─ name: delivery

[Test 8/8]
  Name: Create Economic Event (payment) from Org2
  Org:  org2
  Status: ❌ FAILED.
  Error:  Expected state of Business Object EconomicEvent#0 to be business event, but it was businessEvent, Expected state of Business Object EconomicEvent#1 to be business event, but it was businessEvent

⚠️  Test failed - stopping test suite execution

========================================
  SUMMARY
========================================
Total Tests:  8
Passed:       7 ✅
Failed:       1 ❌
========================================
===== Closing Connections =====
INFO ChaincodeApplication - Closing all connections (2 organizations)
```

---

## Migration from v0.3 to v1.0

### CLI Changes

**v0.3:**
```bash
./ChaincoderTestr --wallet-path wallets/org1 \
                  --identity-name "Org1 Admin" \
                  --connection-profile-path profiles/org1.json \
                  --chaincode-name myChaincode \
                  --test-suite-path test-suite.json
```

**v1.0:**
```bash
./ChaincoderTestr --workspace /path/to/easycc/workspace \
                  --test-suite-path test-suite.json \
                  --chaincode-name myChaincode
```

### Test Suite Changes

**v0.3:** Hardcoded public keys
```json
{
    "payload": {
        "publicKey": "MFkwEwYHKoZI...very long key...",
        "Name": "Agent"
    }
}
```

**v1.0:** Organization placeholders + submittedBy
```json
{
    "submittedBy": "org1",
    "payload": {
        "publicKey": "$org1",
        "Name": "Agent"
    }
}
```

### Running v0.3 Test Suites in v1.0

Use the `--legacy` flag to run old test suites without modification:

```bash
./ChaincoderTestr --legacy \
                  --wallet-path wallets/org1 \
                  --identity-name "Org1 Admin" \
                  --connection-profile-path profiles/org1.json \
                  --chaincode-name myChaincode \
                  --test-suite-path old-test-suite.json
```

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## Related Projects

- **[easycc](https://github.com/AmaVic/easycc)** - Easy Chaincode CLI for Hyperledger Fabric development
- **[B-MERODE](https://merode.be)** - Model-driven engineering approach for enterprise applications

## Citation

If you use this software in your research, please cite:

Laurier, W., Horiuchi, S., & Snoeck, M. (2021). An executable axiomatization of the REA2 ontology. Journal of Information Systems, 35(3), 133-154.

## Contributors and Funding
This project is developed and maintained by Victor Amaral de Sousa. It has been requested and funded by Wim Laurier and Satoshi Horiuchi, respectively from the Université Catholique de Louvain (Belgium) and the University of Chuo (Japan).
