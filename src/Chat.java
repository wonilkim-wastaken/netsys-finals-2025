import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chat {

    public String Chat_ID;
    public Map<String,String> Files =  new HashMap<String,String>();
    public List<User> Users = new ArrayList<User>();
    public List<Message> Messages = new ArrayList<Message>();
    public Chat(String Chat_ID){
        this.Chat_ID = Chat_ID;
    }
    public boolean AddUser(User User){
        if (!CheckUser(User)){
            this.Users.add(User);
            return true;
        }
        return false;
    }
    public boolean DisconnectUser(User User){
        if (CheckUser(User)){
            this.Users.remove(User);
            return true;
        }
        return false;
    }
    public boolean CheckUser(User User){
        return this.Users.contains(User);
    }
    public boolean MessageAllUsers(String Message) throws IOException {
        for (int i = 0; i < Users.size(); i++){
            Users.get(i).UserBufferedWriter.write(Message);
            Users.get(i).UserBufferedWriter.newLine();
            Users.get(i).UserBufferedWriter.flush();
        }
        return true;
    }

    public boolean AddMessage(User User, String Plaintext){
        Message message = new Message(Plaintext, false, User);
        Messages.add(message);
        return true;
    }

    public boolean AddFile(User User, String Filename){
        Message message = new Message(Filename, true, User);
        Messages.add(message);
        Files.put(Filename, Chat_ID +"/files/"+Filename);
        return true;
    }
    public String toString(){
        return Chat_ID + ": " + Users.size() + " connected users.";
    }

}


