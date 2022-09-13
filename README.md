# KMesRework

<!-- ABOUT THE PROJECT -->
## About The Project

![Messenger Home Screen](/src/main/resources/images/homescreen.png?raw=true)

Out of boredom and a school project, I started writing a messenger.
The following things were important to me:

* a good-looking JavaFX frontend to learn something about the lib 
* a pleasant user experience
* security (password hashing and RSA + AES for messages)
* a good grade

<!-- GETTING STARTED -->
## Getting Started

Execute the main method of [ServerTerminal](src/main/java/server/ServerTerminal.java) to start the server and the one of [Main](src/main/java/client/Main.java) to create a client instance.

Note that the default server address in [ClientBackend](src/main/java/client/ClientBackend.java) is defined as **"localhost"** and the default port is **4242**.

https://user-images.githubusercontent.com/88390464/189905622-2bff0615-a9ca-40bd-abb1-c13f890c0eba.mp4

## Recent Changes

1. Message history and contact list
2. Queue messages if a user is offline and send them if they get back online
3. Save images
4. Send images
5. GUI style
6. AES 

<!-- Roadmap -->
## Roadmap

1. Suggest new stuff
