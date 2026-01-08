# Overview
This is a protocol specification documentation which was written to have agreement among team members and make development process simpler.

**Objective**: Allow multiple clients to communicate via server, send files, and sync previous chatting logs
**Architecture**: Client-Server Structure
**Data**:
- Text: ASCII (or UTF-8)
- Commands: specified with `:` 
- Server response code: three digit numbers
# Messages
Every messages are sent in one line and followed by `\n`.
Messages are formulated like following
```
:<command> <ARG1> <ARG2> ...
```
Commands are not needed in case of simple message (message for sending only the text).
# Requests (Client)
Messages will be converted to following structure before they are sent to server.
The request can be sent as JSON for easy implementation.
```
SENDER:<SenderID>,
SENDEE:<SendeeID>,
METHOD:<COMMAND>,
MESSAGE:<message>
```
# Responses (Server)
When server needs to send message to client, it should follow following structure.
```
TYPE:RESPONSE,
CODE:<RESPONSE CODE>,
MESSAGE:<message>
```
Response messages are used to tell client request was successfully handled or not.
# Notifications (Server)
Notifications are special responses that tells client what action happened on server.
Based on notification, client can choose what to do afterwards.  
Notification are sent to clients based on server activities.
```
TYPE:NOTIFICATION,
EVENT:<EVENT_ID>,
SENDER:<SenderID>,
MESSAGE:<message>
```
Notifications are used to tell client *what is happened and what to do*.
# State Definition
**Client Side**
- **connected**: The client is connected to chat
- **disconnected**: The client is not connected to chat
# Commands Definition
**Client -> Server**
- **msg**: send message
  state: connected
```
"<message>"
```
- **connect**: connect client to chat associated to another client(user)
  state: disconnected
```
:connect <ClientID>
```
- **disconnect**: disconnect client from chat
  state: connected
```
:disconnect
```
- **quit**: close chat client
  state: always
```
:quit
```
- **login**: specifies own username to server. Server registers userID when it gets `login` request
```
:login <userID/ClientID>
```
- (Optional) **file**: send file to chat
  state: connected
```
:file <path>
```
# Response Code Definition
This part defines how server should response to client request.
For intuition, the structure imitated HTTP response code.
- **200 (OK)**: Success
- **400 (Bad Request)**: Client request is not formulated correctly (Syntax error)
- **404 (Not found)**: Resource does not exist
- **405 (Method Not Allowed)**: Action invalid for current state
- **500 (Internal Server Error)**: Undefined errors
# Response Message Definition (Neccesary Ones)
- LOGIN_SUCCESS / LOGIN_FAIL / LOGIN_USERNAME_ALREADY_USED
- MESSAGE_SENT / MESSAGE_FAILED
- JOIN_CHAT_SUCCESS / JOIN_CHAT_FAILED
- LEAVE_CHAT_SUCCESS / LEAVE_CHAT_FAILED
- QUIT_APPROVED

# Event Definition
- **CLIENT_JOINED**: Client joined the chat.
- **CLIENT_LEFT**: Client left the chat.
- **MESSAGE_SENT**: Client received the message. / Server sent the message.

# Files
- Files will be saved in Server and then sent to another client
  - Sender(Client) -> Server -> Sendee(Client)
- Clients will be informed when file is incoming
- Clients can agree or disagree to receive file
- When file receiving is declined, sender will be informed.
- Progress bar will be displayed to both sender and sendee side
- Sender should inform server file size and file stream.