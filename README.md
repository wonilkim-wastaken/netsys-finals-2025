# netsys-finals-2025
# Requirements
JDK (Java Development Kit)  
- Download from following [link](https://www.oracle.com/jp/java/technologies/downloads/) if you don't have one

Run following code in terminal after installation
```bash
javac --version
```
If you got something like this, you installed JDK
```bash
javac 25.0.1
```

# How to Compile
1. Clone the repo and forward to src folder
2. Run following command in terminal
```bash
javac *.java
```
This will compile every required files to run

# Running the Server
1. Forward to src folder
2. Run following command in terminal
```bash
java ServerHandler <port>
```
3. Use `Ctrl + C` to terminate server

# Running the Client
1. Forward to src folder
2. Run following command in terminal
```bash
java ClientHandler <ip address> <port> 
```
IP address in the command is IP address of the machine which server is running
3. Run following command to terminate client
```bash
:quit 
```