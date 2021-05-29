import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RobotManager {

    static LinkedHashMap<String,RobotRules> urls = new LinkedHashMap<String, RobotRules>();
    static String  name = "Fa5orgy";
    public static void readRobotTxt(URL url) {

        // Intialize Rules and download robot.txt
        String robotTxtUrl = url.getProtocol()+"://"+ url.getHost() + "/robots.txt";
        RobotRules rules = new RobotRules();
        urls.put(url.getHost(),rules);
        Document doc = null;
        //In case domain doesnt have robot.txt file 404 status code is returned
        try {
            doc = Jsoup.connect(robotTxtUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String text = doc.body().text();

        //Get Index of Crawler Bots Group
        Pattern pattern = Pattern.compile("User-agent: "+ RobotManager.name);
        Matcher matcher = pattern.matcher(text);
        boolean isFound = matcher.find();
        if(!isFound){
            pattern = Pattern.compile("User-agent: \\*");
            matcher = pattern.matcher(text);
            isFound = matcher.find();
        }
        try{

            //Get RobotsTXT Rules
            while (isFound) {
                String group [] = text.substring(matcher.start()).split(" ");
                int numberOfRules = 0;
                boolean groupFinished = false;
                for(int i = 0 ; i<group.length;i++){
                    switch (group[i]){
                        case "User-agent:":
                            if(numberOfRules !=0)
                                groupFinished = true;
                            i++;
                            break;
                        case "Disallow:":
                            numberOfRules++;
                            i++;
                            if(i < group.length)
                                rules.addDisallowed(group[i]);
                            break;
                        case "Allow:":
                            numberOfRules++;
                            i++;
                            if(i < group.length)
                                rules.addAllowed(group[i]);
                            break;
                        }
                    if(groupFinished)
                        break;
                    }
                    isFound = matcher.find();
                }
        }catch(Exception e){
            e.printStackTrace();
            System.out.println(url.toString() + " Is inValid");
        }

    }
    public static boolean  isAllowed(URL url)  {
        
        //Check if hostName was read before
        String hostName = url.getHost();
        if(!urls.containsKey(hostName))
            readRobotTxt(url);

        //Get Path
        String path = url.getPath();
        if(url.getQuery() !=null && !url.getQuery().isEmpty())
            path += "?"+url.getQuery();

        RobotRules rule = urls.get(hostName);

        //Check If DisAllowed
        if(rule.isDisallowed(path)){
            return rule.isAllowed(path);
        }
        return true;
    }
    public static void main(String[] args)  {

        String urls [] = {"https://www.amazon.com/b/?&node=5160028011","https://www.amazon.com/b/?&node=5160028011"};
        try {
            // readRobotTxt(new URL("http://www.amazon.com/"));

            for(int i =0; i< urls.length;i++){
                boolean isAllowed = isAllowed(new URL(urls[i]));
                System.out.println(urls[i] +" " +isAllowed);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
class RobotRules {
    LinkedList<Pattern> allowed = new LinkedList<Pattern>();
    LinkedList<Pattern> disallowed = new LinkedList<Pattern>();

    void  addAllowed(String path){
        //Ignore Directive if path is empty
        if (path.isEmpty()|| !path.startsWith("/"))
            return;
        allowed.add(createPattern(path));
    }
    void addDisallowed(String path){
        //Ignore Directive if path is empty
        if (path.isEmpty() || !path.startsWith("/"))
            return;
        disallowed.add(createPattern(path));
    }
    Pattern createPattern(String path){
        return  Pattern.compile(path.replace("*",".*").replace("?", "\\?"));
    }
    boolean isDisallowed( String path){

        //Check DisAllowed Patterns
        for (int i = 0; i < disallowed.size(); i++) {
            Matcher matcher = disallowed.get(i).matcher(path);
            if(matcher.find())
                return true;
        }
        return false;
    }
    boolean isAllowed( String path){

        //Check Allowed Patterns
        for (int i = 0; i < allowed.size(); i++) {
            Matcher matcher = allowed.get(i).matcher(path);
            if(matcher.find())
                return true;
        }
        return false;
    }
}