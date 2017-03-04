
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
/*
 This class is reducer part. It will combine twitters with completly same userid and hashtags. 
 Before reduce,every row is user_id:hashtags	content
 After reduce, every row is user_id:hashtags content1,content2,content3
 */

public class Q2Reducer {

    public static void main(String[] args) {
        String content = "";
        String idHashtag = "";
        String userid = "";
        String tag = "";
        String currentIdHashtag = "";
        StringBuilder currentContent = new StringBuilder();
        
        try {

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String input;
            while ((input = br.readLine()) != null) {

                String[] parts = input.split("\t");
                //get tha id:hash tag and content. 
                idHashtag = parts[0];
                content = parts[1];
                //if the userid:hashtag(key) is same, append it to the last string. 
                if (currentIdHashtag != null && idHashtag.equals(currentIdHashtag)) { // if the key is still the same
                    currentContent.append("," + content);
                } else {
                    if (currentIdHashtag != null) {
                        // if the key has been changed, which is not equal to last userid:hashtag, output it
                        System.out.println(userid + "\t" + tag + "\t" + currentContent.toString());
                        currentContent = new StringBuilder();
                        currentIdHashtag = idHashtag;
                        String[] newPart = idHashtag.split(":");
                        userid = newPart[0];
                        tag = newPart[1];
                    }
                    currentContent.append(content);
                }
            }

            //output the last line is it is missing. 
            if (currentIdHashtag != null) {
                String[] newPart = currentIdHashtag.split(":");
                userid = newPart[0];
                tag = newPart[1];
                System.out.println(userid + "\t" + tag + "\t" + currentContent.toString());
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}
