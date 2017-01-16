

import java.io.*;
import java.net.Socket;

public class Client implements Runnable {

    // The client socket
    private static Socket clientSocket = null;
    // The output stream
    private static PrintStream os = null;
    // The input stream
    private static DataInputStream is = null;

    private static DataOutputStream dos = null;

    private static BufferedReader inputLine = null;
    private static boolean closed = false;


    public static void main(String[] args) {

        // The default port.
        int portNumber = 8000;

        if (args.length < 1)
        {
            System.out.println("Client is now connected using port number=" + portNumber);
        } else {
            portNumber = Integer.valueOf(args[1]).intValue();
            System.out.println("Client is now connected using port number=" + portNumber);
        }

    /*
     * Open a socket on a given host and port. Open input and output streams.
     */
        try{
            clientSocket = new Socket("localhost", portNumber);
            inputLine = new BufferedReader(new InputStreamReader(System.in));
            os = new PrintStream(clientSocket.getOutputStream());
            is = new DataInputStream(clientSocket.getInputStream());
            dos = new DataOutputStream(clientSocket.getOutputStream());

        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to the host");
        }

    /*
     * If everything has been initialized then we want to write some data to the
     * socket we have opened a connection to on the port portNumber.
     */
        if (clientSocket != null && os != null && is != null) {
            try {

        /* Create a thread to read from the server. */
                new Thread(new Client()).start();
                while (!closed) {
                    String line=inputLine.readLine();
                    String[] splitted= line.split("\\s+");
                    os.println(line.trim());
                    if(splitted.length > 1 && splitted[1].equals("file")){
                        boolean fileExists = false;
                        if(splitted[0].equals("unicast") || splitted[0].equals("blockcast")){
                            File f = new File(splitted[4]);
                            if(f.exists() && !f.isDirectory()){
                                sendFile(splitted[4]);
                                fileExists = true;
                            }
                        }
                        else{
                            File f = new File(splitted[2]);
                            if(f.exists() && !f.isDirectory()){
                                sendFile(splitted[2]);
                                fileExists = true;
                            }
                        }

                        if(!fileExists){
                            System.out.println("This file does not exist. Please enter a valid filename.");
                        }
                    }
                }
        /*
         * Close the output stream, close the input stream, close the socket.
         */     os.close();
                is.close();
                dos.close();
                clientSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendFile(String file) throws IOException {

        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[4096];

        while (fis.read(buffer) > 0) {
            dos.write(buffer);
        }
        dos.writeShort(-1);
        fis.close();
    }
    public void saveFile(String filename) throws IOException {
        FileOutputStream fos;

        fos = new FileOutputStream(filename);

        byte[] buffer = new byte[4096];
        int read = 0;
        int totalRead = 0;
        while((read = is.read(buffer)) > 0) {
            if(buffer[0] == -1 && buffer[1] == -1){
                break;
            }
            totalRead += read;
            fos.write(buffer, 0, read);
        }
        System.out.println("Download Complete!");
        fos.close();
    }


    @SuppressWarnings("deprecation")
    public void run() {
        String responseLine;
        try {
            while ((responseLine = is.readLine()) != null) {


                if(responseLine.startsWith("download "))
                {
                    saveFile(responseLine.split(" ")[2]);
                }

                else
                {
                    System.out.println(responseLine);
                    if (responseLine.indexOf("Bye") != -1)
                        break;
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
