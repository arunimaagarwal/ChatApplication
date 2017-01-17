# ChatApplication
This project is for development of chat application for the course Computer Networks. It would be based on socket programming and would implement the functionalities broadcast, unicast and blockcast of text as well as file.

PROJECT DESCRIPTION

This chat application consists of 2 Java files:

1. Server.java

2. Client.java

In this project, the server accepts connections from multiple clients who can join and leave at any moment. It broadcasts, unicasts and blockcasts messages and files from one client to other appropriate clients. 

The client is implemented using two threads - one thread to interact with the server and other to accept standard input form the user. 

The server is implemented using multiple threads - separate thread for each client connection and one thread to listen to any incoming connection requests. 

COMPILING INSTRUCTIONS

The project has been compiled and tested on Command Prompt, IntelliJ and Eclipse

To execute the program:

Server

java Server

(It takes default port number as 8000) 

OR

java Server <port_no>

Eg: java Server 12345

Client

java Client

(It takes default port number as 8000)

OR

java Client <port_no>

Eg: java Client 12345


As soon as the client joins it would ask to enter the name as:
‘Please enter your name:’

(If the user enters a name already taken by some previous client, it is asked to enter a different name so that it remains unique)


To run the functionalities:

1. broadcast message command: ‘broadcast message <text_message>’ Eg: broadcast message hello

2. broadcast file command: ‘broadcast file <file_name>' Eg: broadcast file input.txt

3. unicast message command: ‘unicast message to <client_name> <text_message>' Eg: unicast message to client1 hello

4. unicast file command: ‘unicast file to <client_name> <file_name>’ Eg: unicast file to client2 input.txt

5. blockcast message command: ‘blockcast message except <client_name> <text_message>’ Eg: blockcast message except client3 hello 

6. blockcast file command: ‘blockcast file except <client_name> <file_name>' Eg: blockcast file except client4 input.txt

(The file should exist in the same sub-folder as the client program)
