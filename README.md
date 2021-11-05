# Third Case - InfraComp

Simplified prototype of a client, repeater and server system in which the communications between client-repeater and repeater-server are encrypted and implemented through sockets.

## Usage

1. Open a terminal at root of this project `Caso3-InfraComp/`.
2. Run `make communication` for running all the components.

If you want to individually run the components, run the following commands at different terminals:

1. For running server, please use `java -cp ./bin app.server.Server SIMETRICO`.
2. For running repeater, please use `java -cp ./bin app.server.Repeater SIMETRICO`.
3. For running client, please use `java -cp ./bin app.client.Client SIMETRICO`.

Remember that you could change `SIMETRICO` to `ASIMETRICO` according to the desired encryption type.

## License

[![License](http://img.shields.io/:license-mit-blue.svg?style=flat-square)](http://badges.mit-license.org)

- **[MIT license](LICENSE)**
- Copyright 2021 © Juan Romero & Juan Alegría
