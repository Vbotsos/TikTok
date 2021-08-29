import java.io.*;
import java.util.Arrays;
import java.io.EOFException;
import java.io.IOException;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.sax.BodyContentHandler;

import org.xml.sax.SAXException;

public class VideoFile implements Serializable {

    public Metadata metadata = new Metadata();
    public String[] metadataNames;
    public transient FileInputStream inputstream = null;
    public transient BodyContentHandler handler = new BodyContentHandler();
    public transient ParseContext pcontext = new ParseContext();
    public MP4Parser MP4Parser = new MP4Parser();
    //File f ;
    String filename,HashTag,channel;
    private byte[] byteArr;


    VideoFile(String filename) {
        this.filename = filename;

    }


    public void openfile() {
        try {
            inputstream = new FileInputStream(new File(filename));
            System.out.println("Success reading file! ");
            convertToChunks(filename);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            MP4Parser.parse(inputstream, handler, metadata, pcontext);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TikaException e) {
            e.printStackTrace();
        }
        metadataNames = metadata.names();

    }


    public void convertToChunks(String filename) {
        FileInputStream is = null;
        try {
            is = new FileInputStream(filename);

            byteArr = IOUtils.toByteArray(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void ToString() {
        System.out.println("Contents of the document:  :" + handler.toString());
        for (String name : metadataNames) {
            System.out.println(name + ": " + metadata.get(name));
        }
    }
    public String getHashTag() {
        return  metadata.get("HashTag");
    }

    public String getChannelName(){return metadata.get("ChannelName");}

    void AddHashTag(String HashTag) {
        metadata.add("HashTag", HashTag);
        this.HashTag=HashTag;

    }

    void AddChannelName(String channel){
        metadata.add("ChannelName",channel);
        this.channel=channel;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public byte[] getByteArr() {

        return byteArr;
    }

    public void setByteArr(byte[] Arr) {
        this.byteArr=new byte[Arr.length];
        for (int i=0;i<Arr.length;i++){
            this.byteArr[i]=Arr[i];
        }

    }
}





