/*
This class sets the response format and data type stored in it. Every result content includes density, time, id and text. 
*/
public class Response implements Comparable<Response> {

//This parameter stores the sentiment density;
    private String density;
//This parameter stores the time string in yyyy-MM-DD, HH-mm-ss format.
    private String time;
//This parameter stores the twitter id.
    private String id;
//This text stores the text after censored. 
    private String text;

    public Response(String density, String time, String id, String text) {
        super();
        this.density = density;
        this.time = time;
        this.id = id;
        this.text = text;
    }

    public String getDensity() {
        return density;
    }

    public void setDensity(String density) {
        this.density = density;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
//This compareTo method overide the original compareTo method. It is designed for sort the multiple content. First sort as density, then time and finally Twitter id.
    public int compareTo(Response res0) {
		// TODO Auto-generated method stub
        //density in descending order
        if (this.density.compareTo(res0.getDensity()) > 0) {
            return -1;
        } else if (this.density.compareTo(res0.getDensity()) < 0) {
            return 1;
        } else {
            //time in ascending order
            if (this.time.compareTo(res0.getTime()) > 0) {
                return 1;
            } else if (this.time.compareTo(res0.getTime()) < 0) {
                return -1;
            } else {
                //id in ascending order
                if (this.id.compareTo(res0.getId()) > 0) {
                    return 1;
                } else if (this.id.compareTo(res0.getId()) < 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }
//The final format for each result. 
    public String toString() {
        return density + ":" + time + ":" + id + ":" + text + "\n";
    }

}
