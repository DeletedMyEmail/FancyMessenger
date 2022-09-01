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

https://user-images.githubusercontent.com/88390464/187886356-53112c49-153f-4d0b-a728-bcfd74251b9c.mp4

<!-- Roadmap -->
## Roadmap

1. Queue messages if a user is offline and send them if they get back online
2. Send larger images
3. Suggest new stuff
