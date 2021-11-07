# Third Case - InfraComp

Simplified prototype of a client, repeater and server system in which the communications between client-repeater and repeater-server are encrypted and implemented through sockets.

## Usage

### Communication prototype running

1. Open a terminal at root of this project `Caso3-InfraComp/`.
2. Execute `java -cp ./bin app.App` for running all the components. This will ask you and start the desired number of clients, repeater and server. For each client, you will be asked to enter their client ID and message ID for being requested. Then you will be able to see the message received by each client with a thread indicator.

If you want to individually visualize the components, run the following commands at different terminals:

1. For running server, please use `java -cp ./bin app.server.Server type`.
2. For running repeater, please use `java -cp ./bin app.repeater.Repeater type`.
3. For running client, please use `java -cp ./bin app.client.Client type clientID messageID`.

- Remember that you could use type `SIMETRICO` or `ASIMETRICO` according to the desired encryption type.
- The clientID is an integer number.
- The messageID is an integer number between 00 and 09.

### Keys generation

When you need to generate a new key, run the following command:

1. For symmetric encryption: `java -cp ./bin app.security.Symmetric type [id]`.
2. For asymmetric encryption: `java -cp ./bin app.security.Asymmetric type [id]`.

- The valid types are `client`, `repeater` or `server`.
- The `id` is an integer number and is only applicable to client keys.

## License

[![License](http://img.shields.io/:license-mit-blue.svg?style=flat-square)](http://badges.mit-license.org)

- **[MIT license](LICENSE)**
- Copyright 2021 © Juan Romero & Juan Alegría
