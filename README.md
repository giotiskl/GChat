# GChat

##Description
GChat is an application I developed purely for learning purposes while trying to get a grip on networking. 
It comes with a (hopefully) functional multi-threaded server and a client. I have packed both the client
and the server in the same project, to set them up as different projects check the instructions that follow.

* **Author:** Giotis Kaltsikis
* **E-mail:** giotisgr@gmail.com

I have tried to include comprehensive documentation throughout the project. 
Executable JARs included for both client and server in the project's root folder.

### Instructions
* Create a new project for the client application. Include packages: ***com.giotis_kal.gchatclient***, ***gchatdata*** and ***gchat.warning_dialog***
* Create a new project for the server application. Include packages: ***com.giotis_kal.gchatserver***, ***gchatdata***

## Project Structure
### GChat Client (pkg com.giotis_kal.gchatclient)
* **GChatClient** - maintains an internal connection to the server and is responsible for handling user and server I/O using object streams

### GChat Warning Dialog
Since GChat client's UI is developed in JavaFX, this package includes a quick modal dialog control used to display warning messages. It should be thus
ignored, by those interested solely in the networking aspects of this application.

### GChat Server (pkg com.giotis_kal.gchatserver)
* **GChatServer** - is the implementation of a multithreaded TCP/IP chat server. Its UI was hacked quickly together in Swing.

### GChat Data (pkg gchatdata)
* **ChatMessage** - represents the message objects exchanged between server and user. Apart from the actual message
 it also contains info regarding the username of the person sending the message.
* **Poison** - is used to end communication between server and client (either in the case of a user disconnect or when the server is taken down).
In the latter case it is flagged as appropriate for shutting the server down.