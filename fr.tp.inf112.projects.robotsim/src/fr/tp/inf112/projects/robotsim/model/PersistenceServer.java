package fr.tp.inf112.projects.robotsim.model;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.*;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.model.impl.AbstractCanvasPersistenceManager;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class PersistenceServer {
    public static void main(String[] args) {
        int port = 65500; // Server will listen on this port

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                // Accept incoming client connections
                Socket socket = serverSocket.accept();
                // Handle the connection in a separate thread
                Runnable reqProcessor = new PersistenceRequestProcessor(socket);
                new Thread(reqProcessor).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class PersistenceRequestProcessor implements Runnable {
    private Socket socket;

    public PersistenceRequestProcessor(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // Read request
            InputStream inStream = socket.getInputStream();
            OutputStream outStream = socket.getOutputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inStream);
            ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);

            Object recvObject = objectInputStream.readObject();

            if (recvObject instanceof String){ // Assuming read() is called and send back
                String canvasId = (String) recvObject;
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outStream);
                System.out.println("A string received, deserializing from designated file...");
                try {
                        final InputStream fileInputStream = new FileInputStream(canvasId);
                        final InputStream bufInputStream = new BufferedInputStream(fileInputStream);
                        final ObjectInputStream localObjectInputStrteam = new ObjectInputStream(bufInputStream);
                	Canvas canvas = (Canvas) localObjectInputStrteam.readObject();
                    objectOutputStream.writeObject(canvas);
                    objectOutputStream.flush();
                }
                catch (ClassNotFoundException | IOException ex) {
                    throw new IOException(ex);
                }
            }
            else if (recvObject instanceof Canvas) {
                Canvas canvas = (Canvas) recvObject;
                System.out.println("A canvas received, serializing to a file...");
                try (
                        final OutputStream fileOutStream = new FileOutputStream(canvas.getId());
                        final OutputStream bufOutStream = new BufferedOutputStream(fileOutStream);
                        final ObjectOutputStream objOutStream = new ObjectOutputStream(bufOutStream);
                ) {
                    objOutStream.writeObject(canvas);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}