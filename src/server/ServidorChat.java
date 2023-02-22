package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorChat {
    // Definimos el puerto por el que el servidor escuchará las conexiones
    private static final int PORT = 1234;
    // Creamos dos listas para guardar los BufferedWriter de los clientes conectados
    // y sus respectivos nombres de usuario
    private static List<BufferedWriter> writers = new ArrayList<>();
    private static List<String> usernames = new ArrayList<>();
    // Definimos las variables para el servidor y el socket
    public static ServerSocket server;
    public static Socket socket;

    public static void main(String[] args) throws IOException {
        // Creamos el servidor y lo iniciamos en el puerto definido
        server = new ServerSocket(PORT);
        System.out.println("Servidor iniciado en el puerto " + PORT);

        // El servidor siempre está a la espera de nuevas conexiones
        while (true) {
            // Cuando se establece una conexión, se crea un nuevo socket
            socket = server.accept();
            System.out.println("Nuevo cliente conectado");

            // Se crea un BufferedReader para recibir los mensajes del cliente
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Se lee el nombre de usuario del cliente
            String nickname = reader.readLine();
            System.out.println("Nuevo cliente: " + nickname);

            // Se crea un BufferedWriter para enviar mensajes al cliente y se añade a la
            // lista de writers
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writers.add(writer);
            // Se añade el nombre de usuario a la lista de usernames
            usernames.add(nickname);

            // Enviamos los nombres de usuario a todos los clientes conectados
            enviarUsuariosConectados();

            // Creamos un nuevo hilo para atender al cliente
            new Thread(new HiloServidorChat(reader, nickname)).start();
        }

    }

    // Método que envía un mensaje a todos los clientes conectados
    public static void enviarMensaje(String message) {
        System.out.println(message);
        for (BufferedWriter writer : writers) {
            try {
                writer.write(message + "\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Método que envía a todos los clientes conectados la lista de usuarios
    // conectados
    private static void enviarUsuariosConectados() {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Usuarios conectados: ");
        for (String username : usernames) {
            messageBuilder.append(username).append(", ");
        }
        String message = messageBuilder.toString();
        message = message.substring(0, message.length() - 2); // Eliminamos la última coma y espacio
        enviarMensaje(message);
    }

	private static class HiloServidorChat implements Runnable {
	    private BufferedReader reader; // El lector de entrada para el cliente
	    private String nickname; // El nombre de usuario del cliente
	
	    public HiloServidorChat(BufferedReader reader, String nickname) {
	        this.reader = reader;
	        this.nickname = nickname;
	    }
	
	    @Override
	    public void run() {
	        enviarMensaje(nickname + " se ha conectado."); // Notifica a todos los clientes que un nuevo usuario se ha
	                                                       // conectado
	        try {
	            while (true) { // Bucle para leer los mensajes enviados por el cliente
	                String message = reader.readLine();
	                if (message == null) {
	                    break; // Si el cliente se desconecta, se sale del bucle
	                }
	                enviarMensaje(nickname + ": " + message); // Envia el mensaje del cliente a todos los clientes
	                                                          // conectados
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            writers.removeIf(writer -> writer.equals(reader)); // Elimina al cliente de la lista de escritores
	            usernames.remove(nickname); // Elimina al cliente de la lista de nombres de usuario
	            try {
	                reader.close(); // Cierra el lector de entrada
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	            // Actualiza la lista de usuarios conectados para todos los clientes
	            enviarUsuariosConectados(); 
	            // Notifica a todos los clientes que el cliente se ha desconectado
	            enviarMensaje(nickname + " ha abandonado el chat.");
	        }
	    }
	}
}
