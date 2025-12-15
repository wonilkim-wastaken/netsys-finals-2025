import java.net.Socket;

public class Chat {

    public String user1;
    public String user2;
    public Chat(String user, String user2) {
        this.user1 = user;
        this.user2 = user2;
    }

    public String FileContents;

    public String GetOther(String user){
        if (user1.equals(user2)){
            return user1;
        }
        return user2;
    }
}


