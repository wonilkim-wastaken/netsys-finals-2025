public class Message {
    public Message(String messsage, boolean containsFile, User user){
        Message =  messsage;
        ContainsFile = containsFile;
        Username = user.Username;
    }
    public String Message;
    public boolean ContainsFile;
    public String Username;

    public String toString(){
        return Username + ": " + Message;
    }
}
