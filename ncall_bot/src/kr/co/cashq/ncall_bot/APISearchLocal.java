package kr.co.cashq.ncall_bot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

public class APISearchLocal {
	public static String search_naver(String str)
	{
	    String clientId = Env.getInstance().CLIENT_ID;//애플리케이션 클라이언트 아이디값";
        String clientSecret = Env.getInstance().CLIENT_SECRET;//애플리케이션 클라이언트 시크릿값";
		String result="";
        try {
            String text = URLEncoder.encode(str, "UTF-8");
            String apiURL = "https://openapi.naver.com/v1/search/local?query="+ text; // json 결과
            //String apiURL = "https://openapi.naver.com/v1/search/blog.xml?query="+ text; // xml 결과
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode==200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            result=response.toString();
        } catch (Exception e) {
            System.err.println(e);
        }

		return result;
	}

	public static String get_json(String str)
	{
		String result="0";
    
		try {
		    JSONParser jsonParser = new JSONParser();            
			JSONObject jsonObject = (JSONObject) jsonParser.parse(str);
			result=jsonObject.get("total").toString();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
            
        	
		return result;
	}
/*
	public static void main(String[] args) {
		String searchstr="";
		System.out.println("ncall_bot");
		searchstr=search_naver("05049932681");
		System.out.println(searchstr);
		System.out.println("ncall_bot : "+get_json(searchstr));
	}
	*/
}
