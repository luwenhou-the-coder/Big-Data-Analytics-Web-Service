
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
/*
This class is to do the map part. 
*/
public class Q2Mapper {
	static LinkedHashMap<String, String> scoreMap = new LinkedHashMap<String, String>();
	static List<String> stopWordList = new LinkedList<String>();
	static List<String> banWordList = new LinkedList<String>();
	static LinkedList<String> t_id = new LinkedList<String>();
//to get words lists from url .
	public static void initialFiles() {
		String line = new String();
		URL url;
		try {
			//store the affin words in the scoreMap. 
			url = new URL("https://cmucc-datasets.s3.amazonaws.com/15619/f15/afinn.txt");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
			while ((line = bufferedReader.readLine()) != null) {
				String[] words = line.split("\t");
				//words[0] is word and words[1] is the score. 
				scoreMap.put(words[0], words[1]);
				
			}
			bufferedReader.close();

		} catch (Exception e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			//store the stop words in the stopWordsList.
			url = new URL("https://s3.amazonaws.com/cmucc-datasetss16/common-english-word.txt");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
			line = bufferedReader.readLine();
			String[] words = line.split(",");
			stopWordList = Arrays.asList(words);
	
			bufferedReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			//store the banned words in the banWordsList.
			url = new URL("https://cmucc-datasets.s3.amazonaws.com/15619/f15/banned.txt");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
			while ((line = bufferedReader.readLine()) != null) {
				line = decodeROT13(line);
				banWordList.add(line);
			}

			bufferedReader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	//decode the word which is coded in ROT13.
	public static String decodeROT13(String word){
		//word = word.toLowerCase();
		char[] wordArray = word.toCharArray();
		for(int i = 0; i < word.length(); i++){
			if (wordArray[i] >= 'a' && wordArray[i] <= 'm'){
				wordArray[i] = (char) (wordArray[i] + 13);
			}
			else if(wordArray[i] >= 'n' && wordArray[i] <= 'z')
				wordArray[i] = (char) (wordArray[i] - 13);
		}
		return new String(wordArray);
	}
	//if the word is in the scoreMap, get the score of it. 
	public static int parseScore(String word) {
		int score = 0;
		if (scoreMap.containsKey(word)){
			score = Integer.parseInt(scoreMap.get(word));
		
		}
		return score;
	}
	//if the word is a stop word, return 1. 
	public static int judgeStopWords(String word) {
		int count = 0;
		for (String stopWord : stopWordList) {
			if (word.equals(stopWord)) {
				count = 1;
				break;
			}

		}
		return count;
	}
	//calculate the density.
	public static String calculateSenDensity(String text) {
		double density = 0;
		//transfer into lower case
		text = text.toLowerCase();
		int sentimentScore = 0;
		//split by  non-alphanumeric character(s)
		String[] words = text.split("[^a-zA-Z0-9]");

		int wordsLength = words.length;
		int stopWordsNum = 0;
		for (int i = 0; i < words.length; i++) {
			//there are "" at the end of words array after split. ignore this part
			if(words[i].equals("")){
				wordsLength = wordsLength - 1;
			}
			//get the score
			sentimentScore += parseScore(words[i]);
			//get the stopword number
			stopWordsNum += judgeStopWords(words[i]);
			
		}
		//get the ewc
		int effectivewords = wordsLength - stopWordsNum;
		if (effectivewords == 0) {
			return "0.000";
		}
		//calculate density
		density = (double)sentimentScore /(double) effectivewords;
		//set the format and round. 
		BigDecimal b = new BigDecimal(density);
		DecimalFormat d = new DecimalFormat("0.000");
		String densityStr = d.format(b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
	
		return densityStr;
	}
	//convert time into the yyyy-MM-dd HH-mm-ss format
	public static String convertTime(String time) {
		String newTime = "";
		try {
			String timesub = time.substring(4);
			String year = time.substring(time.length() - 4);
			SimpleDateFormat formatter = new SimpleDateFormat("MMM dd HH:mm:ss ZZZZZ yyyy");
			formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date d = formatter.parse(timesub);
			SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
			formatter2.setTimeZone(TimeZone.getTimeZone("UTC"));
			newTime = formatter2.format(d);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newTime;

	}

	//if the word is in the banword list, use * 
	public static String banWord(String word) {
		char[] str = word.toCharArray();

		for (int i = 1; i < str.length - 1; i++) {

			str[i] = '*';
		}
		String censorStr = new String(str);

		return censorStr;
	}
	//censore
	public static String censorWords(String text) {
		Map<String, String> replaceMap = new HashMap<>();
		//text = text.toLowerCase();
		String[] words = text.split("[^a-zA-Z0-9]");
		String censor = null;
		for (int i = 0; i < words.length; i++) {
			String lowCaseWord = words[i].toLowerCase();
			//judge whehter the word should be banned. 
			if (banWordList.contains(lowCaseWord)) {

				replaceMap.put(words[i], banWord(words[i]));

			}

		}
		censor = text;

		Iterator<Map.Entry<String, String>> iterator = replaceMap.entrySet().iterator();
		while (iterator.hasNext()) {

			Map.Entry<String, String> entry = iterator.next();
			//replace the word with the word after processing. 
			censor = text.replaceAll(entry.getKey(), entry.getValue());

		}

		return censor;
	}
	//this method is for tranfer string as a byte array string. 
	public static String transferByteArrayString(String str) {
		byte[] sby;
		String byteString = new String();
		try {
			//get the utf-8 bytes of the string. 
			sby = str.getBytes("UTF-8");
			StringBuilder strbuild = new StringBuilder();
			//make it as a new string with byte array. 
			for (int i = 0; i < sby.length; i++) {

				strbuild.append(sby[i] + " ");
			}
			byteString = strbuild.toString();
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return byteString;
	}

	public static void main(String[] args) throws FileNotFoundException {
		
		try {
			initialFiles();
			
			BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
			String line = "";

			while ((line = buffer.readLine()) != null) {
				try {
					// JSONParser parser = new JSONParser();
					JSONObject twitter = (JSONObject) new JSONParser().parse(line);
					// malformed cannot be a json object

					if (twitter == null) {
						continue;
					}

					Long id = (Long) twitter.get("id");
					String id_str = (String) twitter.get("id_str");

					// malformed missing id and id_str
					if (id == null && id_str == null) {
						continue;
					}
					// remove duplicate if id is null and id_str is not null,
					// the id value can be created from id_str
					if (id == null)
						id = Long.parseLong(id_str);
					if (t_id.contains(id.toString())) {
						continue;
					}

					String text = (String) twitter.get("text");
				
				    String time = (String) twitter.get("created_at");
					
					time = convertTime(time);

					// malformed missing text, time
					if (text == null && time == null) {
						continue;
					}

					JSONObject user = (JSONObject) twitter.get("user");
					String user_id = (String) user.get("id_str");
					if(user_id.equals("") || user_id == null)
						continue;

					JSONObject entities = (JSONObject) twitter.get("entities");
					// malformed missing entities
					if (entities == null) {
						continue;
					}

					t_id.add(id.toString());
					JSONArray hashtags = (JSONArray) entities.get("hashtags");
					//calculate density
					String density = calculateSenDensity(text);
					//do the censor 
					String newText = censorWords(text);
				

					String finalResult;
					String tagsbyte = "";

					//remove twitters if  the hashtag is null. 
					if(hashtags.size() == 0)
						continue;
					if (hashtags.size() != 0) {
						//get every hash tag the content has. 
						for (int i = 0; i < hashtags.size(); i++) {
							JSONObject hashobj = (JSONObject) hashtags.get(i);
							String tag = (String) hashobj.get("text");
							tagsbyte = "#" + transferByteArrayString(tag) + tagsbyte;
						}
					}
					String response = density + ":" + time + ":" + id + ":" + transferByteArrayString(newText);
					
					finalResult = user_id + ":" + tagsbyte + "\t" + response;
					System.out.println(finalResult);
				} catch (Exception e) {	
					continue;
				
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
