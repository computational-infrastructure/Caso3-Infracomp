package app;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;

public class App {
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        ArrayList<String> clientes = new ArrayList<String>();
        System.out.println("\n¿Qué tipo de cifrado quiere utilizar?");
        System.out.println(" - SIMETRICO");
        System.out.println(" - ASIMETRICO");
        String tipo = s.nextLine();
        if (!tipo.equalsIgnoreCase("SIMETRICO") && !tipo.equalsIgnoreCase("ASIMETRICO")) {
            System.out.println("Tipo de cifrado no válido");
            System.exit(1);
        }
        System.out.println("\n¿Cuántos Clientes van a recibir mensajes?");
        int numClientes = Integer.parseInt(s.nextLine());
        try {
            String servidor = "java -cp ./bin app.server.Server " + tipo;
            String repetidor = "java -cp ./bin app.repeater.Repeater " + tipo;
            Process pS = Runtime.getRuntime().exec(servidor);
            Process pR = Runtime.getRuntime().exec(repetidor);
            for (int i = 0; i < numClientes; i++) {
                String cliente = "java -cp ./bin app.client.Client " + tipo + " ";
                System.out.println("\nDigite el ID del cliente número " + (i + 1) + ":");
                String id = s.nextLine();
                Integer.parseInt(id);
                cliente += id + " ";
                System.out.println("Digite el ID del mensaje que quiere recuperar el cliente:");
                String mensaje = s.nextLine();
                if (Integer.parseInt(mensaje) > 9 || Integer.parseInt(mensaje) < 0) {
                    System.err.println("The message ID must be between 00 and 09");
                    i -= 1;
                    continue;
                }
                cliente += mensaje;
                clientes.add(cliente);
                if (tipo.equalsIgnoreCase("SIMETRICO")) {
                    Runtime.getRuntime().exec("java -cp ./bin app.security.Symmetric client " + id);
                } else if (tipo.equalsIgnoreCase("ASIMETRICO")){
                    Runtime.getRuntime().exec("java -cp ./bin app.security.Asymmetric client " + id);
                }
            }
            System.out.println("\n----------------");
            System.out.println("A continuación iniciará la ejecución de los threads de todos los clientes registrados");
            Thread.sleep(500);
            s.close();
            Proceso.barrera = new CyclicBarrier(numClientes + 1);
            new App().execute(clientes);
            Proceso.barrera.await();
            System.out.println("\n----------------");
            System.out.println("Se ha finalizado la ejecución del prototipo de comunicación");
            pS.destroy();
            pR.destroy();
            Thread.sleep(500);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute(ArrayList<String> comandos) {
        for (int i = 0; i < comandos.size(); i++) {
            Proceso ec = new Proceso(i, comandos.get(i));
            new Thread(ec).start();
        }
    }

    public class Proceso implements Runnable {
        private int prIndex;
        private String executable;
        private static CyclicBarrier barrera;

        public Proceso(int k, String cmd) {
            prIndex = k;
            executable = cmd;
        }

        public void run() {
            try {
                Process child = Runtime.getRuntime().exec(executable);
                BufferedReader br = new BufferedReader(new InputStreamReader(child.getInputStream()));
                for (String s = br.readLine(); s != null; s = br.readLine()) {
                    System.out.println("[" + prIndex + "] " + s);
                }
                br.close();
                Proceso.barrera.await();
            } catch (Exception ioex) {
                System.err.println("IOException for process #" + prIndex + ": " + ioex.getMessage());
            }
        }
    }
}
