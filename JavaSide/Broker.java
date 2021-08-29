import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Broker {
    static ArrayList<Integer> PORTS=new ArrayList();
    HashMap<String, List<byte[]>> map = new HashMap<String, List<byte[]>>();
    Queue<byte[]> queue2 =new LinkedList<>();
    HashMap<String,List<Value>> Storage=new HashMap<>();
    ArrayList<Value> Videos=new ArrayList<>();
    public static boolean flag=true;
    HashMap<String,List<Value>> channels=new HashMap<>();
    ArrayList<String> Chnames=new ArrayList<String>();

    public HashMap getHash(){
        return channels;
    }
    synchronized void sentChannels(Socket s) throws IOException {
        DataOutputStream dOut = new DataOutputStream(s.getOutputStream());
        int Size=Chnames.size();
        dOut.writeInt(Size); // write length of the message
        System.out.println("SENT number  of names: "+ Size);
        dOut.flush();
            String name;
        int i=0;
        while (i<Size){
            name= Chnames.get(i);
            dOut.writeUTF(name);
            dOut.flush();
            i++;
        }
    }
    synchronized void sentVideoChannels(Socket s) throws IOException{
        DataInputStream dIn = new DataInputStream(s.getInputStream());
        String channelName=dIn.readUTF();
        System.out.println(channelName);

        System.out.println(channels.get(channelName).size());

        DataOutputStream dOut = new DataOutputStream(s.getOutputStream());
        dOut.writeUTF(channels.get(channelName).get(0).video.getHashTag());
        dOut.flush();

        for (int i=0;i<channels.size();i++) {
           System.out.println(channels.get(channelName).get(0).video.getHashTag());
            System.out.println(channels.get(channelName).get(0).video.getChannelName());
            if (channels.get(channelName).get(0).video.getChannelName().equals(channelName)) {
                int bytesRead=0;

                System.out.println("Length of video: "+ channels.get(channelName).get(0).video.getByteArr().length);
                dOut.writeInt(channels.get(channelName).get(0).video.getByteArr().length); // write length of the video
                dOut.flush();

                System.out.println("Uploading Video...");
                while ((bytesRead) <channels.get(channelName).get(0).video.getByteArr().length) {
                    dOut.write(channels.get(channelName).get(0).video.getByteArr()[bytesRead]);

                    // write the message
                    if (bytesRead < channels.get(channelName).get(0).video.getByteArr().length) {
                        bytesRead++;
                    } else {
                        break;
                    }
                }
                dOut.flush();
                System.out.println("Upload Of the Video END");
            }
        }
    }
    synchronized void ReceiveVideo(Socket s) throws IOException, ClassNotFoundException {
        System.out.println("[BROKER]:Receiving video from PUBLISHER");

        String channelName;
        DataInputStream dIn = new DataInputStream(s.getInputStream());
        channelName=dIn.readUTF();
        System.out.printf("channelName: %s\n", channelName);

        if(!Chnames.contains(channelName)){
            Chnames.add(channelName);
        }

        String HashTag;
        HashTag=dIn.readUTF();
        System.out.printf("Sent from the broker the Video with HashTag: %s\n", HashTag);

        int Numvideos = dIn.readInt(); // read length of incoming message
        System.out.println("We Read number Of videos of video: " + Numvideos);

        int count=0;
        while (count<Numvideos) {

           //dIn.reset();
            int length = dIn.readInt(); // read length of incoming message
            System.out.println("We Read length of video: " + length);

            System.out.println(" Read Value");

            byte[] input = new byte[length];
            if (length > 0) {
                int bytesRead=0;
                while(bytesRead<length){
                    input[bytesRead]=dIn.readByte();
                    bytesRead++;
                }
                System.out.println("lele: "+input.length);

                Value v=new Value(HashTag);
                v.video.AddHashTag(HashTag);
                v.video.AddChannelName(channelName);
                v.video.setByteArr(input);

                if (channels.containsKey(channelName)){
                    channels.get(channelName).add(v);
                }else{
                    channels.put(channelName,new ArrayList<Value>());
                    channels.get(channelName).add(v);
                }

                if (Storage.containsKey(HashTag)) {
                    Storage.get(HashTag).add(v);
                } else {
                    Storage.put(HashTag, new ArrayList<Value>());
                    Storage.get(HashTag).add(v);
                }
            } else {
                System.out.println("The video is empty! ");
            }
            count++;
        }

        System.out.println("Video Received!");
        dIn.close();
    }
    boolean IsConnected(Socket socket) throws IOException {
        System.out.println("Is connected with PORT: "+socket.getPort());
        return socket.isConnected();
    }
    String  ReadHashTag(Socket s) throws IOException {
        String HashTag;
        DataInputStream dIn = new DataInputStream(s.getInputStream());
        HashTag=dIn.readUTF();
        System.out.println("HashTag read!: "+HashTag +"\n");
        return HashTag;
    }
    public  static int getBroker(String input) {
        int r=0;
        String s=getMd5(input);
        String p="127.0.01"+String.valueOf(8082);
        String p1="127.0.01"+String.valueOf(8086);
        String p2="127.0.01"+String.valueOf(8090);
        p=getMd5(p);
        p1=getMd5(p1);
        p2=getMd5(p2);
        if(s.compareTo(p1)<=0) {
            r=2;
        }else if(s.compareTo(p)<=0) {
            r=1;
        }else if(s.compareTo(p2)<=0) {
            r=3;
        }else r=1;
        return r;
    }
    public static String getMd5(String input) {
        try {

            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // digest() method is called to calculate message digest
            //  of an input digest() return array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    void pull(String hashtag){
        if(map.containsKey(hashtag)) {
            for(int i=0;i<map.get(hashtag).size();i++) {
                queue2.add(map.get(hashtag).get(i));
            }
        }else{
            System.out.println("this hashtag doesnt exist");
        }
    }
    public ArrayList<Value> getVideos(String hashtag) {
        if(Storage.containsKey(hashtag)) {
            for(int i=0;i<Storage.get(hashtag).size();i++) {
                Videos.add(Storage.get(hashtag).get(i));
            }
        }else{
            System.out.println("this hashTag doesnt exist");
        }
        return Videos;
    }
    synchronized void PullSentVideo(Socket s, ArrayList<Value> Videos, String HashTag) throws IOException, ClassNotFoundException {
        DataOutputStream dOut = new DataOutputStream(s.getOutputStream());
        dOut.writeUTF(HashTag);
        dOut.flush();

        dOut.writeInt(Videos.size());//write the number of the videos
        System.out.println("The number of videos: "+Videos.size());
        dOut.flush();


        int VideoFile=0;
        while (Videos.size()>VideoFile) {
            int bytesRead = 0;

            System.out.println("Length of video: "+ Videos.get(VideoFile).getVideo().getByteArr().length);
            dOut.writeInt(Videos.get(VideoFile).getVideo().getByteArr().length); // write length of the video
            dOut.flush();

            System.out.println("Uploading Video...");
            while ((bytesRead) <Videos.get(VideoFile).getVideo().getByteArr().length) {
                dOut.write(Videos.get(VideoFile).getVideo().getByteArr()[bytesRead]);

                // write the message
                if (bytesRead < Videos.get(VideoFile).getVideo().getByteArr().length) {
                    bytesRead++;
                } else {
                    break;
                }
            }
            dOut.flush();
            VideoFile++;
            System.out.println("Upload Of the Video END");
        }
    }
     int  NotifyPublisher(Socket s) throws IOException {
        String HashTag;
        DataInputStream dIn = new DataInputStream(s.getInputStream());
        HashTag=dIn.readUTF();
        return getBroker(HashTag);
    }


    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
        boolean flag = true;
        Broker b=new Broker();
        /* read Ports Here */
        Scanner scan = new Scanner(System.in);
        System.out.println("Give the number of the file with ports that you want to read: \n");
        int number = scan.nextInt();
        System.out.println("[BROKER"+number+"]");

        Scanner scanner = new Scanner(new File("PortsForBroker"+number+".txt"));
        int [] ports = new int [7];
        int i = 0;
        while(scanner.hasNextInt())
        {
            ports[i++] = scanner.nextInt();
        }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        //Thread 0 listen  first Consumer
        Thread Consumer1 = new Thread(new Runnable() {
            private Socket clientSocket;
            ServerSocket server = null;

            public void run() {
                try {
                    server = new ServerSocket(ports[0]);
                    server.setReuseAddress(true);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                while (true) {
                    Socket client = null;
                    try {
                        client = server.accept();
                        clientSocket = client;
                        System.out.println("[BROKER] :CONNECTED WITH Consumer1 on port: " + ports[0]);
                        new Thread(ConsumerHandler).start();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            Thread ConsumerHandler = new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println(ports[0]);
                    System.out.println("Welcome Consumer! \n");
                    try {

                        DataInputStream dIn = new DataInputStream(clientSocket.getInputStream());
                        String check=dIn.readUTF();
                        System.out.println("check "+check);

                        if(check.equals("1")){
                            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        String hashtag=in.readLine();
                        System.out.println("HashTag read!: "+hashtag +"\n");

                        int broker=getBroker(hashtag);
                        System.out.println("Now sending the right broker! "+broker);

                        DataOutputStream dOut = new DataOutputStream(clientSocket.getOutputStream());
                        if(broker==number) {
                            if (broker == 1) {
                                dOut.writeInt(8080);
                                dOut.flush();
                            } else if (broker == 2) {
                                dOut.writeInt(8084);
                                dOut.flush();
                            } else {
                                dOut.writeInt(8088);
                                dOut.flush();
                            }
                            ArrayList<Value> videos = b.getVideos(hashtag);
                            if (!videos.isEmpty()){
                                System.out.println(b.Storage.get(hashtag).get(0).filename);
                            System.out.println("Now i am sending video!: ");
                            b.PullSentVideo(clientSocket, videos, hashtag);
                            System.out.println("Video Sent!!!");
                        }
                        }else{
                            if(broker==1) {
                                dOut.writeInt(8080);
                                dOut.flush();
                            }else if(broker==2){
                                dOut.writeInt(8084);
                                dOut.flush();
                            }else{
                                dOut.writeInt(8088);
                                dOut.flush();
                            }
                        }}else if(check.equals("2")){
                            b.sentChannels(clientSocket);
                        }else if(check.equals("3")){
                            b.sentVideoChannels(clientSocket);
                        }

                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
        });

        //Thread 1 listen first Publisher
        Thread Publisher1 = new Thread(new Runnable() {
            Socket clientSocket;
            ServerSocket server = null;

            public void run() {
                try {
                    server = new ServerSocket(ports[2]);
                    server.setReuseAddress(true);
                    while (true) {
                        Socket client = server.accept();
                        clientSocket = client;
                        System.out.println("[BROKER] :CONNECTED WITH PUBLISHER1 on port: " + ports[2]);
                        new Thread(PublisherHandler).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Thread PublisherHandler=new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        int broker=b.NotifyPublisher(clientSocket);
                        DataOutputStream dOut = new DataOutputStream(clientSocket.getOutputStream());

                        if(broker==number){
                            if(broker==1) {
                                dOut.writeInt(8082);
                                dOut.flush();
                            }else if(broker==2){
                                dOut.writeInt(8086);
                                dOut.flush();
                            }else{
                                dOut.writeInt(8090);
                                dOut.flush();
                            }
                            b.ReceiveVideo(clientSocket);

                        }else{
                            if(broker==1) {
                                dOut.writeInt(8082);
                                dOut.flush();
                            }else if(broker==2){
                                dOut.writeInt(8086);
                                dOut.flush();
                            }else{
                                dOut.writeInt(8090);
                                dOut.flush();
                            }

                        }

                        System.out.println("The right broker that publisher should connect is: "+broker);
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
        });

        //Thread 2 listen broker(Server Broker)
        Thread Broker1=new Thread(new Runnable() {
            ServerSocket server = null;
            Socket clientSocket;
            @Override
            public void run() {

                try {
                    server = new ServerSocket(ports[4]);
                    server.setReuseAddress(true);
                    System.out.println("[BROKER"+number+"] :BROKER CONNECTED ON PORT: " + ports[4]);
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (true){
                    Socket client = null;
                    try {
                        client = server.accept();
                        clientSocket = client;
                        new Thread(Broker1handler).start();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            Thread Broker1handler=new Thread(new Runnable() {
                @Override
                public void run() {

                }
            });
        });

        //Thread 3 reply broker (client broker)
        Thread Broker2=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(60000);
                    System.out.println("Wait Ended: \n");
                    new Thread(Broker2handler).start();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Thread Broker2handler=new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket s=new Socket("127.0.0.1",ports[5]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });



        Consumer1.start();
        Publisher1.start();
        Broker1.start();
        Broker2.start();
    }

}