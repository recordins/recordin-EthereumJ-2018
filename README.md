# Welcome to Record'in Blockchain Platform


# About

**Recordins** is a pure-java **Blockchain data and documents storage platform** based on Ethereum protocol ([Ethereumj](https://github.com/ethereum/ethereumj)).
It is designed to work for private or semi-public consortium governance models. 

The additional features to a standard blockchain are:

- **Support for a data model** like in a standard database engine, with relations between objects
- **Authentication Layer**
- **Multi-level security layers based on ACLs** over object classes, user owned or individual objects
- **Support for external documents** storage
- **GDPR compliant**

It can work in a **standalone** or in a **multi-node networking** environement. Each node can have its own configuration:

- Share or not its users database, to allow login on other nodes or just on selected ones
- Share or not its external documents database, to allow cloning the documents on all nodes or just on selected ones
- Is a mining node
- Is a read only node 
 

Recordins also provides the capability of building software modules able to extend the platform behaviour, **like any standard ERP**, for following actions on objects:

- validation (with the datamodel)
- create
- write
- delete
- check secutity

Finally, the platform provides access to the data an all the actions through 2 ways:

- **WEB interface**
- **REST-JSON** interfaces (the web interface relies on these URLs)

# Running Recordins from the Source Code

Clone the repository, then use following Gradle tasks to run the blockchain software.

- runStandalone – Creates a local blockchain node, mainly for development purpose. It updates the contents of the “etc” folder with the contents of “etc_standalone”.

- runStandaloneReset –  Same as the runStandalone task, but re-initializes the local contents of the blockchain node.
 
- runNetworkFirstNode – Initiates a local blockchain node, intended to be the first running node of a network. It updates the contents of the “etc” folder with the contents of “etc_network_first”.

- runNetworkNode – Runs a new blockchain node intended to synchronize with the already initiated node (initiated with the runNetworkFirstNode task). It updates the contents of the “etc” folder with the contents of “etc_network_node”.

- runNetworkFirstNodeReset– Same as the r runNetworkFirstNode task, but re-initializes the local contents of the blockchain node.

- runNetworkNodeReset – Same as the runNetworkNode task, but re-initializes the local contents of the blockchain node.


# Running a Recordins release

Just download the release from github: https://github.com/recordins/recordin/releases

Extract the archive, and run **recordin.sh** on Linux, or **recordin.bat** on Windows.

Then open your Internet browser, and go to the following URL: [http://localhost:8080](http://localhost:8080)

**Beware** that the first time, the platform takes about 10mn to initiate.
Record'in requires at least IE 11 for the web interface to run correctly.
 
# Contact
You can find more informations and documentation on our website: [www.recordins.com](https://www.recordins.com).

# License
Record'in is released under the [AGPL-V3 license](LICENSE).

