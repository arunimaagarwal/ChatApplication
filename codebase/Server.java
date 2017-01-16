
import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;

public class Server
{

    // The server socket.
    private static ServerSocket serverSocket = null;
    // The client socket.
    private static Socket clientThreadsocket = null;

    // This chat server can accept up to clientThreadsCount clientThreads' connections.
    private static final int clientThreadsCount = (int)Math.sqrt(Integer.MAX_VALUE);
    private static final clientHandler[] clientThreads = new clientHandler[clientThreadsCount];

    public static void main(String args[])
    {
        // The default port number.
        int port = 8000;
        if (args.length < 1)
        {
            System.out.println("Server is now using port number=" + port);
            System.out.println("To stop it, press <CTRL><C>.");
        }
        else
        {
            port = Integer.valueOf(args[0]).intValue();
            System.out.println("Server is now using port number=" + port);
            System.out.println("To stop it, press <CTRL><C>.");
        }

        /*
         * Open a server socket on the port
         */
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e)
        {
            System.out.println(e);
        }

        /*
         * Create a client socket for each connection and pass it to a new client
         * thread.
         */
        while (true)
        {
            try
            {
                clientThreadsocket = serverSocket.accept();
                int i = 0;
                for (i = 0; i < clientThreadsCount; i++)
                {
                    if (clientThreads[i] == null)
                    {
                        (clientThreads[i] = new clientHandler(clientThreadsocket, clientThreads,i)).start();
                        System.out.println("Client "+(i+1)+" is now connected...");
                        break;
                    }
                }
            } catch (IOException e)
            {
                System.out.println(e);
            }
        }
    }
}

class clientHandler extends Thread
{

    private String clientName = null;
    private DataInputStream is = null;
    private DataOutputStream dos = null;
    private PrintStream os = null;
    private Socket clientThreadsocket = null;
    private final clientHandler[] clientThreads;
    private int clientThreadsCount;
    private int no = 1;
    private String filename=null;

    public clientHandler(Socket clientThreadsocket, clientHandler[] clientThreads, int no)
    {
        this.clientThreadsocket = clientThreadsocket;
        this.clientThreads = clientThreads;
        clientThreadsCount = clientThreads.length;
        this.no = no;
    }

    public void saveFile(String filename) throws IOException {
        FileOutputStream fos;

        fos = new FileOutputStream(filename);
        byte[] buffer = new byte[4096];

        int read = 0;
        while((read = is.read(buffer)) > 0) {
            if(buffer[0] == -1 && buffer[1] == -1){
                break;
            }
            fos.write(buffer, 0, read);
        }
        System.out.println("File transfer complete");
        fos.close();
        //dis.close();
    }

    public void sendFile(String file) throws IOException {

        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[4096];

        while (fis.read(buffer) > 0) {
            dos.write(buffer);
        }

        dos.writeShort(-1);
        fis.close();

    }

    @SuppressWarnings("deprecation")
    public void run()
    {
        int clientThreadsCount = this.clientThreadsCount;
        clientHandler[] clientThreads = this.clientThreads;
        int k=0;

        try
        {
      /*
       * Create input and output streams for this client.
       */
            is = new DataInputStream(clientThreadsocket.getInputStream());
            dos=new DataOutputStream(clientThreadsocket.getOutputStream());
            os = new PrintStream(clientThreadsocket.getOutputStream());
            String name;

            while (true)
            {
                os.println("Please enter your name:");
                name = is.readLine().trim();
                break;
            }

      /* Welcome the new the client. */
            synchronized (this)
            {
                for (int i = 0; i < clientThreadsCount; i++)
                {
                    if(i!=0 && clientThreads[i-1].clientName.equals(name))
                    {
                        os.println("Please enter another username:");
                        name=is.readLine().trim();
                    }

                    if (clientThreads[i] != null && clientThreads[i] == this)
                    {
                        clientName =name;
                        break;
                    }
                }
                os.println("To exit, press <CTRL><C>.");
                for (int i = 0; i < clientThreadsCount; i++)
                {
                    if (clientThreads[i] != null && clientThreads[i] != this)
                    {
                        clientThreads[i].os.println(name + "  has joined the group !");
                    }
                }
            }
      /* Start the conversation. */
            while (true) {
                String line = is.readLine();
                if(line == null)
                {
                    break;
                }

        /* If the message is private sent it to the given client. */
                if (line.startsWith("unicast message to ")) {
                    String[] words = line.split("\\s", 5);
                    if (words.length > 1 && words[4] != null) {
                        words[4] = words[4].trim();
                        if (!words[4].isEmpty()) {
                            synchronized (this) {
                                for (int i = 0; i < clientThreadsCount; i++) {
                                    if (clientThreads[i] != null && clientThreads[i] != this
                                            && clientThreads[i].clientName != null
                                            && clientThreads[i].clientName.equals(words[3])) {
                                        clientThreads[i].os.println("<" + name + "> " + words[4]);
                                        k=i;
                                        k++;
                                        break;
                                    }
                                }
                                this.os.println("Message Sent");
                                System.out.println("Client " + (no + 1) + " sent a unicast message to client "+k);
                            }
                        }
                    }
                }

                /* The message is broadcast to all clientThreads except one. */
                else if(line.startsWith("blockcast message except ")) {
                    String[] words = line.split("\\s", 5);
                    if (words.length > 1 && words[4] != null) {
                        words[4] = words[4].trim();
                        if (!words[4].isEmpty())
                        {
                            synchronized (this)
                            {
                                for (int i = 0; i < clientThreadsCount; i++)
                                {
                                    if (clientThreads[i] != null && clientThreads[i] != this
                                            && clientThreads[i].clientName != null
                                            && (!(clientThreads[i].clientName.equals(words[3]))))
                                    {
                                        clientThreads[i].os.println("<" + name + "> " + words[4]);
                                    }
                                    else if (clientThreads[i] != null && clientThreads[i] != this
                                            && clientThreads[i].clientName != null
                                            && (clientThreads[i].clientName.equals(words[3])))
                                    {
                                        k=i;
                                        k++;
                                    }
                                }
                                this.os.println("Message Sent");
                                System.out.println("Client " + (no + 1) + " sent a blockcast message excluding Client "+k);
                            }
                        }
                    }
                }

                /* The message is public, broadcast it to all other clientThreads. */
                else if(line.startsWith("broadcast message ")) {
                    String[] words = line.split("\\s", 3);
                    if (words.length > 1 && words[2] != null) {
                        words[2] = words[2].trim();
                        if (!words[2].isEmpty())
                        {
                            synchronized (this) {
                                for (int i = 0; i < clientThreadsCount; i++) {
                                    if (clientThreads[i] != null && clientThreads[i] != this && clientThreads[i].clientName != null) {
                                        clientThreads[i].os.println( "<" + name + "> " + words[2]);

                                    }
                                }
                                this.os.println("Message Sent");
                                System.out.println("Client " + (no + 1) + " sent a broadcast message.");
                            }
                        }
                    }
                }
                /* The file is public, broadcast it to all other clientThreads. */
                else if(line.startsWith("broadcast file ")) {
                    String[] words = line.split("\\s", 3);
                    if (words.length > 1 && words[2] != null) {
                        words[2] = words[2].trim();
                        filename=words[2];
                        saveFile(filename);
                        if (!words[2].isEmpty())
                        {
                            synchronized (this) {
                                for (int i = 0; i < clientThreadsCount; i++) {
                                    if (clientThreads[i] != null && clientThreads[i] != this && clientThreads[i].clientName != null) {

                                            clientThreads[i].os.println("download "+clientThreads[i].clientName+" "+filename);
                                            clientThreads[i].sendFile(filename);
                                    }
                                }
                                this.os.println("File Sent");
                                System.out.println("Client " + (no + 1) + " sent a broadcast file.");
                            }
                        }
                    }
                }
                /* If the file is private sent it to the given client. */
                else if(line.startsWith("unicast file to ")) {
                    String[] words = line.split("\\s", 5);
                    if (words.length > 1 && words[4] != null) {
                        words[4] = words[4].trim();
                       filename=words[4];
                        saveFile(filename);
                        if (!words[4].isEmpty()) {
                            synchronized (this) {
                                for (int i = 0; i < clientThreadsCount; i++) {
                                    if (clientThreads[i] != null && clientThreads[i] != this
                                            && clientThreads[i].clientName != null
                                            && clientThreads[i].clientName.equals(words[3])) {
                                        clientThreads[i].os.println("download "+clientThreads[i].clientName+" "+filename);
                                        clientThreads[i].sendFile(filename);
                                        k=i;
                                        k++;
                                        break;
                                    }
                                }
                                this.os.println("File Sent");
                                System.out.println("Client " + (no + 1) + " sent a unicast file to client "+k);
                            }
                        }
                    }
                }
                /* The file is broadcast to all clientThreads except one. */
                else if(line.startsWith("blockcast file except ")) {
                    String[] words = line.split("\\s", 5);
                    if (words.length > 1 && words[4] != null) {
                        words[4] = words[4].trim();
                       filename=words[4];
                        saveFile(filename);
                        if (!words[4].isEmpty()) {
                            synchronized (this) {
                                for (int i = 0; i < clientThreadsCount; i++) {
                                    if (clientThreads[i] != null && clientThreads[i] != this
                                            && clientThreads[i].clientName != null
                                            && (!(clientThreads[i].clientName.equals(words[3]))))
                                    {
                                        clientThreads[i].os.println("download "+clientThreads[i].clientName+" "+filename);
                                        clientThreads[i].sendFile(filename);

                                    }
                                    else if (clientThreads[i] != null && clientThreads[i] != this
                                            && clientThreads[i].clientName != null
                                            && (clientThreads[i].clientName.equals(words[3])))
                                    {
                                        k=i;
                                        k++;
                                    }
                                }
                                this.os.println("File Sent");
                                System.out.println("Client " + (no + 1) + " sent a blockcast file except client "+k);
                            }
                        }
                    }
                }
                else{
                    this.os.println("Unrecognized functionality! Please re-input");
                }

            }
            synchronized (this) {
                for (int i = 0; i < clientThreadsCount; i++) {
                    if (clientThreads[i] != null && clientThreads[i] != this
                            && clientThreads[i].clientName != null) {
                        clientThreads[i].os.println("The user " + name
                                + " has left!");
                    }
                }
                System.out.println("It seems like Client "+(no+1)+" is disconnected");
            }
            os.println("You can no longer send or receive messages");

      /*
       * Clean up. Set the current thread variable to null so that a new client
       * could be accepted by the server.
       */
            synchronized (this) {
                for (int i = 0; i < clientThreadsCount; i++) {
                    if (clientThreads[i] == this) {
                        clientThreads[i] = null;
                    }
                }
            }
      /*
       * Close the output stream, close the input stream, close the socket.
       */   is.close();
            os.close();
            dos.close();
            clientThreadsocket.close();

        } catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}

