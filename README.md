# Kotlin Substrate Client
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

substrate-client-kotlin is client library to interact with a substrate-based chain.
It uses the API available from the RPC endpoint only (no sidecar).
As of today it provides the following functionality:

* compatible with substrate 2.0
* ed25519 wallet creation
* get account info (balance)
* sign extrinsic and send (immortal era)
* estimate fee

what is currently **not** supported

* sr25519 wallet
* mortal era extrinsic

## Using the library

1. Add the JitPack repository.
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

2. Add the substrate-client dependency
```
dependencies {
        implementation 'com.github.NodleCode:substrate-client-kotlin:1.0'
}
```


## Running the tests over custom rpc endpoints

The library comes with a set of JUnit Test to ensure that the api  
provided by this library works properly

In order to test that this library works over your own substrate rpc  
endpoints, you need to provide the chains configuration that will be  
used by the tests.

Simply create a file in

```
substrate-client/src/test/resources/rpc-test.config
```

This file must be a CSV (without header) and must contain 3 columns per lines:
* rpc endpoint (e.g. wss://rpc.polkadot.io)
* alice wallet mnemonic
* bob wallet mnemonic

Alice and Bob wallet must be provisioned with enough token to be able
to perform the test and pay the fees. When testing sending transaction,
the test will always send token from the wallet with the most token.

Here is an example of such file (wallets and rpc doesn't exists):
```
wss://mainnet.substrate-chain.io,smoke key grief belt gather absurd open attend keep flip hollow popular,total arch interest inmate book cigar primary long mixture party practice old
wss://testnet.substrate-chain.io,pumpkin poem tuition scatter elder moral hockey valley health head joke stem,pupil opinion unhappy nerve adult lunch dolphin famous angry draw soap like
```

You can then run the tests with gradle:

```
./gradlew test
```

The test report will be generated in `substrate-client/build/reports/test/index.html`

## Additional Notes

This is a work in progress and comes with no warranty.
contribution are welcome. If you have any question, ideas or if you found a bug, please open an issue!