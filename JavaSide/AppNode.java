import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Scanner;

public class AppNode {
    static final String IP="127.0.0.1";

    public void connect(int port){
        try
        {
            System.out.println(IP+" "+port);
            System.out.println("Is up with PORT: "+port+" and IP: "+IP);
        }
        catch(Exception e)
        {
            System.out.println("Error");
        }
    }
    public void disconnect(Socket s){
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void push(String HashTag,Socket s,int videos,String ChannelName) throws IOException, ClassNotFoundException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        //sent channelName to Broker
        DataOutputStream dOut = new DataOutputStream(s.getOutputStream());
        dOut.writeUTF(ChannelName);
        dOut.flush();


        //sent HashTag to Broker
        dOut.writeUTF(HashTag);
        dOut.flush();

        //Sent number of videos to broker
        dOut.writeInt(videos); // write length of the message
        System.out.println("SENT number  of videos: "+ videos);
        dOut.flush();


        int count=0;
        while(count<videos) {

            System.out.println("give the filename of the video that you want to upload: \n");
            String filename=reader.readLine();

            Value v=new Value(filename+".mp4");
            v.setChannelname(ChannelName);
            v.video.openfile();
            v.video.AddHashTag(HashTag);

            dOut.writeInt(v.video.getByteArr().length); // write length of the message
            System.out.println("SENT Length of video: " + v.video.getByteArr().length);
            dOut.flush();

//            ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
//            os.reset();
//            os.writeObject(v);
//            System.out.println("Value sent...");
//            os.flush();

System.out.println("Uploading Video...");
            int bytesRead = 0;

            while ((bytesRead) < v.video.getByteArr().length) {
                dOut.writeByte(v.video.getByteArr()[bytesRead]);
                if (bytesRead < v.video.getByteArr().length) {
                    bytesRead++;
                } else {
                    break;
                }
            }
            dOut.flush();
            count++;
        }

        dOut.flush();
        dOut.close();
        s.close();
        System.out.println("Upload Of the Video ENDED");
    }
    public void chunk(byte[] b,String filename,int number) throws IOException {
        File someFile = new File("example.mp4");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filename+number+"New.mp4");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            System.out.println("Writing obj ");
            for (int i = 0; i < b.length; i++) {
                fos.write(b[i]);
            }
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static Socket RightBroker(Socket socket, int port) throws IOException {
        socket=new Socket(IP, port);
        System.out.println("New Socket created: "+port);
        return socket;
    }
    public int NotifyBrokersForHashtags(String hashTag, Socket s) throws IOException {
        DataOutputStream dOut = new DataOutputStream(s.getOutputStream());
        dOut.writeUTF(hashTag);
        dOut.flush();

        DataInputStream dIn = new DataInputStream(s.getInputStream());
        int RightBrokerPort=dIn.readInt();
        System.out.println(RightBrokerPort);
        return RightBrokerPort;
    }
    public void pullVideos(int port){

        try {
            ServerSocket ss = new ServerSocket(port);
            Socket s = ss.accept();
            InputStream is = s.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);

            Queue<Value> queue2 = (Queue<Value>)ois.readObject();

            is.close();
            s.close();
            ss.close();
        }catch(Exception e){System.out.println(e);}
    }
    void ConsumerReceiveVideo(Socket s) throws IOException, ClassNotFoundException {

        String HashTag;
        DataInputStream dIn = new DataInputStream(s.getInputStream());
        HashTag=dIn.readUTF();
        System.out.printf("Sent from the broker the Video with HashTag: %s\n", HashTag);


        int NumberOfVideosToReceive=dIn.readInt();
        System.out.println("We Read the number of videos : "+NumberOfVideosToReceive);

        int count =0;
        while (count<NumberOfVideosToReceive) {
            int length = dIn.readInt(); // read length of incoming message
            System.out.println("We Read length of video: " + length);


            System.out.println(" Reading the Value...");
//            ObjectInputStream is = new ObjectInputStream(s.getInputStream());
//            Value v = (Value) is.readObject();

            System.out.println("Now reading the chunks! ");
            byte[] input = new byte[length];
            if (length > 0) {
                int bytesRead=0;
                while(bytesRead<length){
                    input[bytesRead]=dIn.readByte();
                    bytesRead++;
                }
                System.out.println("input length: " + input.length);
                chunk(input, HashTag,count);
                System.out.println("The Video with HashTag: "+HashTag+" Received!");
            }
            count++;
        }
        dIn.close();
    }


    public static void main(String[] args) throws InterruptedException {

        //PUBLISHER THREAD
        Thread Publisher = new Thread(new Runnable() {
            @Override
            public void run() {
                AppNode nodeP=new AppNode();
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

                System.out.println("[Publisher]");
                Scanner scan = new Scanner(System.in);
                System.out.println("give port for the Publisher\n");

                int port = scan.nextInt();
                Socket s= null;
                try {
                    s = new Socket(IP, port);
                    nodeP.connect(port);

                    System.out.println("give the your channelName: \n");
                    String ChannelName=reader.readLine();

                    System.out.println("give the number of  videos that you want to upload: \n");
                    int NumOfVideos= Integer.parseInt(reader.readLine());

                    System.out.println("give the HashTag of the video that you want to upload: \n");
                    String HashTag=reader.readLine();

                    int RightBroker=nodeP.NotifyBrokersForHashtags(HashTag,s);

                    Socket newSocket = null;
                    if(port!=RightBroker) {
                        System.out.println(1);
                        s.close();

                        newSocket = new Socket(IP, RightBroker);
                        AppNode NewNodeP=new AppNode();
                        NewNodeP.connect(RightBroker);
                        NewNodeP.NotifyBrokersForHashtags(HashTag,newSocket);

                        System.out.println("Connected to the right Broker");
                        NewNodeP.push(HashTag,newSocket,NumOfVideos,ChannelName);
                        NewNodeP.disconnect(newSocket);
                    }else {
                        System.out.println(2);
                        nodeP.push(HashTag,s,NumOfVideos,ChannelName);
                        nodeP.disconnect(s);
                    }

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        //CONSUMER THREAD
        Thread Consumer = new Thread(new Runnable() {
            @Override
            public void run() {
                AppNode nodeC=new AppNode();
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(System.in));

                System.out.println("[CONSUMER]");
                Scanner scan = new Scanner(System.in);
                System.out.println("give port for the Consumer\n");
                int port = scan.nextInt();
                Socket s= null;
                try {
                    s = new Socket(IP, port);
                    nodeC.connect(port);
                    System.out.println("You want to upload(type upload) or watch video(type watch)? \n");
                    if(reader.readLine().equals("watch")) {
                        DataOutputStream dOut = new DataOutputStream(s.getOutputStream());
                        dOut.writeUTF("1");
                        dOut.flush();

                        System.out.println("Give the HashTag of the video that you want to watch: \n");
                        // takes input from terminal
                        String HashTag=reader.readLine();


                        PrintWriter  out = new PrintWriter(s.getOutputStream(), true);
                        out.println(HashTag);
                        out.flush();
                        //Now read the port of the correct Broker that you should connect
                        DataInputStream dIn = new DataInputStream(s.getInputStream());
                        int RightBrokerPort=dIn.readInt();

                        System.out.println(RightBrokerPort+" Broker-Consumer right broker \n");

                        if(port!=RightBrokerPort) {
                            s.close();
                            Socket newSocket = null;
                            newSocket=RightBroker(newSocket, RightBrokerPort);

                            //senting again hashTag
                            out = new PrintWriter(newSocket.getOutputStream(), true);
                            out.println(HashTag);
                            out.flush();

                            dIn = new DataInputStream(newSocket.getInputStream());
                            RightBrokerPort=dIn.readInt();
                            System.out.println(RightBrokerPort);

                            nodeC.ConsumerReceiveVideo(newSocket);

                        }else{
                            nodeC.ConsumerReceiveVideo(s);
                            System.out.println("Video Received!!!");
                        }
                    }else{
                        System.out.println("Give the HashTag of the video that you want to upload: \n");
                        String HashTag=reader.readLine();
                        VideoFile video =new VideoFile(HashTag);
                        Value v=new Value(HashTag+".mp4");
                    }
                    nodeC.disconnect(s);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        Publisher.start();
        Publisher.join();

        Consumer.start();
        Consumer.join();
    }


}