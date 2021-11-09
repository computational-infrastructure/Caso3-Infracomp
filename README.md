# Third Case - InfraComp

Simplified prototype of a client, repeater and server system in which the communications between client-repeater and repeater-server are encrypted and implemented through sockets.

## Usage

### Communication prototype running

1. Open a terminal at root of this project `Caso3-InfraComp/`.
2. Make sure that you have the project compiled. Use this command for it `find $JAVA_./src/app/ -name '*.java' -exec javac -d ./bin {} +` or your prefered Java workflow.
3. Execute App.java or use `java -cp ./bin app.App` to run all the components. This will ask you the encryption type for all of the following communications, and will start the desired number of clients, repeater and server. For each client, you will be asked to enter their client ID and message ID that they are requesting. Then, the program will execute different processes to automatically generate all the needed keys for the communication. Finally, you will be able to see the message received by each client followed by a thread indicator.

If you want to individually visualize the components, run the following commands at different terminals:

1. Make sure that all the client, server, and repeater keys to be used are generated. If you need to create new keys, follow the instructions at the [keys generation section](#keys-generation).
2. To run the server, please use `java -cp ./bin app.server.Server type`.
3. To run the repeater, please use `java -cp ./bin app.repeater.Repeater type`.
4. To run a single client, please use `java -cp ./bin app.client.Client type clientID messageID`.

- Valid encryption types: `SIMETRICO` or `ASIMETRICO`. The chosen type must be the same one for all the components in an execution.
- The clientID is an integer number.
- The messageID is an integer number between 00 and 09.

### Keys generation

If you need to generate a new key, run the following commands:

1. For symmetric encryption: `java -cp ./bin app.security.Symmetric type [id]`.
2. For asymmetric encryption: `java -cp ./bin app.security.Asymmetric type [id]`.

- The valid types are `client`, `repeater` or `server`.
- The `id` is an integer number and is only applicable to client keys.

## License

[![License](http://img.shields.io/:license-mit-blue.svg?style=flat-square)](http://badges.mit-license.org)

- **[MIT license](LICENSE)**
- Copyright 2021 © Juan Romero & Juan Alegría

