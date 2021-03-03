package com.example.guessstarapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    String url = "https://www.imdb.com/list/ls082599715/?sort=list_order,asc&mode=detail&page=1&ref_=nmls_vm_dtl";

    private ImageView imageViewStar;

    private ArrayList<String> urls;
    private ArrayList<String> names;
    private ArrayList<Button> buttons;

    private int numberOfQuestion;
    private int numberRightAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageViewStar = findViewById(R.id.imageViewStar);
        Button button0 = findViewById(R.id.button0);
        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        Button button3 = findViewById(R.id.button3);
        buttons = new ArrayList<>();
        buttons.add(button0);
        buttons.add(button1);
        buttons.add(button2);
        buttons.add(button3);
        ImageView imageViewStar = findViewById(R.id.imageViewStar);
        urls = new ArrayList<>();
        names = new ArrayList<>();
        getContent();
        playGame();
    }


    private void getContent() {
        DownloadContentTask task = new DownloadContentTask();
        try {
            String context = task.execute(url).get();
            String start = "<div class=\"lister-item mode-detail\">";
            String finish = "<div class=\"footer filmosearch\">";
            Pattern pattern = Pattern.compile(start + "(.*?)" + finish);
            Matcher matcher = pattern.matcher(context);
            String splitContent = "";
            while (matcher.find()) {
                splitContent = matcher.group(1);
            }
            Pattern patternImage = Pattern.compile("src=\"(.*?)\"");
            Pattern patternName = Pattern.compile("alt=\"(.*?)\"");
            Matcher matcherImage = patternImage.matcher(splitContent);
            Matcher matcherName = patternName.matcher(splitContent);
            while (matcherImage.find()) {
                urls.add(matcherImage.group(1));
            }
            while (matcherName.find()) {
                names.add(matcherName.group(1));
            }
            for (String s : urls) {
                Log.i("Result", s);
            }


        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void playGame() {
        generateQuestion();
        DownloadImageTask task = new DownloadImageTask();
        try {
            Bitmap bitmap = task.execute(urls.get(numberOfQuestion)).get();
            if (bitmap != null) {
                imageViewStar.setImageBitmap(bitmap);
                for (int i = 0; i < buttons.size(); i++) {
                    if (i == numberRightAnswer) {
                        buttons.get(i).setText(names.get(numberOfQuestion));
                    } else {
                        int wrongAnswer = generateWrongAnswer();
                        buttons.get(i).setText(names.get(wrongAnswer));
                    }
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void generateQuestion() {
        numberOfQuestion = (int) (Math.random() * names.size());
        numberRightAnswer = (int) (Math.random() * buttons.size());
    }


    private int generateWrongAnswer() {
        return (int) (Math.random() * names.size());
    }

    public void onClickAnswer(View view) {
        Button button = (Button) view;
        String tag = button.getTag().toString();
        if (Integer.parseInt(tag) == numberRightAnswer) {
            Toast.makeText(this, "Верно!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Неверно, правильный ответ: " + names.get(numberOfQuestion), Toast.LENGTH_SHORT).show();
        }
        playGame();
    }


    private static class DownloadContentTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    result.append(line);
                    line = reader.readLine();
                }
                return result.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

            }

            return null;
        }
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

            }

            return null;
        }
    }

}