import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Main {
    private static HttpURLConnection connection;

    public static void main(String[] args) {
        //Method 1: (if you arent using Java 11, this is your only option) java.net.HttpURLConnection
        //Establish Buffered Reader and String buffer to read the response
        BufferedReader reader;
        String line;
        StringBuffer responseContent = new StringBuffer();

        try {
            //URL url = new URL("https://jsonplaceholder.typicode.com/albums");
            String inputCurrency = "GBP";
            String outputCurrency = "USD";
            String conversionDate = "11/7/2019";
            conversionDate = normalizeDate(conversionDate);
            String conversionDateDayLater = addDayToDate(conversionDate);
            double inputAmount = 300.75;
            String urlWithParams = "https://api.exchangeratesapi.io/history?start_at=" + conversionDate + "&end_at=" + conversionDateDayLater + "&symbols=" + inputCurrency + "," + outputCurrency + "&base=" + inputCurrency;
            URL url = new URL(urlWithParams);
            connection = (HttpURLConnection) url.openConnection();

            //Request Setup
            connection.setRequestMethod("GET");
            //set 5 second timeout for read and connect
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int status = connection.getResponseCode();
            //System.out.println(status);

            if (status > 200) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }
            //while loop going through each line of the failed response to build the response message
            while ((line = reader.readLine()) != null) {
                responseContent.append(line);
            }
            //reading response is complete, closer the reader
            reader.close();
            System.out.println(responseContent.toString());
            //parse(responseContent.toString());
            double outputAmount = parseConversion(responseContent.toString(), inputCurrency, outputCurrency, conversionDate, conversionDateDayLater, inputAmount);
            outputAmount = Math.round(outputAmount * 100.0) / 100.0;
            System.out.println(outputAmount);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }

//        //Method 2: java.net.http.HttpClient - Java 11 async handling
//        //Build the HTTP Client
//        HttpClient client = HttpClient.newHttpClient();
//        //Build the HtppRequest
//        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://jsonplaceholder.typicode.com/albums")).build();
//        //Set the request using the client
//        //HttpResponse.BodyHandlers says we want to receive the response as string - CompletableFuture datatype
//        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
//                //:: means lambda expresion - we want to use the body method of the HTTP class on the previous result
//                .thenApply(HttpResponse::body)
//                //Call parse using a lambda expression...this was added after testing with .thenAccept
//                .thenApply(Main::parse)
//                //.thenAccept(System.out::println)
//                //Return the results
//                .join();
    }

//    public static String parse(String responseBody){
//        //Create a new JSON array for the response
//        JSONArray albums = new JSONArray(responseBody);
//        //iterate through the JSON objects of the array to extrac the data based on keys
//        for(int i=0;i<albums.length();i++){
//            JSONObject album = albums.getJSONObject(i);
//            int id = album.getInt("id");
//            int userId = album.getInt("userId");
//            String title = album.getString("title");
//            System.out.println(id + " " + title + " " + userId);
//        }
//        return null;
//    }

    public static double parseConversion(String responseBody, String inputCurrency, String outputCurrency, String conversionDate, String conversionDateDayLater, double inputAmount) {
        //Create a new JSON array for the response
        //JSONArray albums = new JSONArray(responseBody);
        JSONObject response = new JSONObject(responseBody);
        double outputCurrencyRate = 0.0;
        try {
            if (response.has("rates")) {
                JSONObject rates = response.getJSONObject("rates");
                if (rates.has(conversionDate)) {
                    JSONObject startDate = rates.getJSONObject(conversionDate);
                    outputCurrencyRate = startDate.getDouble(outputCurrency);
                } else if (rates.has(conversionDateDayLater)) {
                    JSONObject startDate = rates.getJSONObject(conversionDateDayLater);
                    outputCurrencyRate = startDate.getDouble(outputCurrency);
                }
            }
        } catch (Exception e) {
            //checking for rate on next date for return
            e.printStackTrace();
        }
        return inputAmount * outputCurrencyRate;
    }

    public static String addDayToDate(String oldDate) {
        //define new SimpleDateFormat object with target pattern for date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(oldDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //Increment the date by 1 day
        c.add(Calendar.DAY_OF_MONTH, 1);
        //store the new calendar date in string
        String newDate = sdf.format(c.getTime());
        System.out.println(newDate);
        return newDate;
    }

    public static String normalizeDate(String strDate) {
        //define new SimpleDateFormat object with target pattern for date
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Date dtDate = new Date();
        try {
            dtDate = sdf.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        String strOutput = sdf.format(dtDate);
        System.out.println(strOutput);
        return strOutput;
    }
}

