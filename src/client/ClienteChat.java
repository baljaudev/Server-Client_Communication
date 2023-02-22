package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ClienteChat extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    private JTextArea textArea;
    private JTextField textField;
    private JButton sendButton;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private JScrollPane scrollPane;
    private JButton exitButton;

public ClienteChat(String nickname, String host, int port) {
        // Configurar la ventana
        setTitle("Chat - " + nickname);
        getContentPane().setLayout(new BorderLayout());

        // Agregar la JTextArea y JScrollPane para mostrar los mensajes
        textArea = new JTextArea(20, 50);
        textArea.setEditable(false);
        scrollPane = new JScrollPane(textArea);
        scrollPane.setSize(400, 600);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Agregar los botones y el campo de texto para enviar mensajes
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        
        exitButton = new JButton("Salir");
        exitButton.addActionListener(this);
        inputPanel.add(exitButton, BorderLayout.WEST);

        textField = new JTextField();
        textField.setSize(100, 100);
        inputPanel.add(textField, BorderLayout.CENTER);

        sendButton = new JButton("Enviar");
        sendButton.addActionListener(this);
        inputPanel.add(sendButton, BorderLayout.EAST);

        getContentPane().add(inputPanel, BorderLayout.SOUTH);

        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        try {
            // Establecer la conexión con el servidor
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // Enviar el nickname al servidor
            writer.write(nickname + "\n");
            writer.flush();
        } catch (ConnectException ce) {
            // Manejar el caso donde no se puede conectar con el servidor
            ce.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "SERVIDOR NO CONECTADO\n" + ce.getMessage(),
                    "<<MENSAJE DE ERROR :1>>",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Configurar un thread para recibir los mensajes del servidor
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean seguir = true;
                while (seguir) {
                    try {
                        String message = reader.readLine();
                        if (message == null) {
                            break;
                        }
                        // Mostrar el mensaje en la JTextArea
                        textArea.append(message + "\n");
                    } catch (SocketException se) {
                        // Manejar el caso donde el servidor se desconecte
                        se.printStackTrace();
                        JOptionPane.showMessageDialog(null,
                                "SERVIDOR INTERRUMPIDO\n" + se.getMessage(),
                                "<<MENSAJE DE ERROR :1>>",
                                JOptionPane.ERROR_MESSAGE);
                        seguir = false;
                        System.exit(0);
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            // Enviar el mensaje al servidor
            writer.write(textField.getText() + "\n");
            writer.flush();
            textField.setText("");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        if (e.getSource() instanceof JButton) { 
            if (e.getActionCommand().equals("Salir")) { 
                // Manejar el caso donde el usuario quiere salir del programa
                int resp = JOptionPane.showConfirmDialog(this, "Se va a cerrrar la aplicacion, Desea cerrarla?",
                        "Confirmar salida", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (resp == JOptionPane.YES_NO_OPTION) {
                    System.exit(0);
                }
            }
        }
    }

    public static void main(String[] args) {
        //Pedir el nickname del usuario
        String nickname = JOptionPane.showInputDialog(null, "Introduce tu nickname:");

        new ClienteChat(nickname, "localhost", 1234);
    }
}