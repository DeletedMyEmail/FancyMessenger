# KMesRework

<!-- ABOUT THE PROJECT -->
## About The Project

![Messenger Home Screen](/src/main/resources/images/homescreen.png?raw=true)

Out of boredom and a school project, I started writing a messenger.
The following things were important to me:

* a good looking JavaFX frontend to learn something about the lib 
* a pleasant user experience
* security (check out the roadmap)
* a good grade

<!-- GETTING STARTED -->
## Getting Started

First of all, this project is based on Java.net sockets to establish all connections. Execute the main method of [InputHandler](src/main/java/server/InputHandler.java) to start the server and the one of [ClientBackend](src/main/java/client/ClientBackend.java) to create a client instance.

Note that the server host is defined by default in [ClientBackend](src/main/java/client/ClientBackend.java) as "localhost".

<!-- Roadmap -->
## Roadmap

1. Queue messages if a user is offline and send them if they get back online
2. Message encryption
3. Suggest new stuff
