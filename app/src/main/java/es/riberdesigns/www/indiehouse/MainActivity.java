package es.riberdesigns.www.indiehouse;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static android.view.View.OnClickListener;

public class MainActivity extends AppCompatActivity {
    private EditText nom;
    private ListView llista_noms;
    private ArrayAdapter<String> lv;
    private ArrayList<String> list1 = new ArrayList<>();
    ClipboardManager myClipboard;

    ClipData myClip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nom = (EditText) findViewById(R.id.nom);
        Button submit = (Button) findViewById(R.id.submit);
        myClipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        llista_noms = (ListView) findViewById(R.id.llista);


        lv = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list1);
        new Consultardatos().execute("http://riberdesigns.es/prova.php");
        assert submit != null;
        submit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                new CargarDatos().execute("http://riberdesigns.es/prova.php?nom=" + nom.getText().toString());
                lv.clear();
                new Consultardatos().execute("http://riberdesigns.es/prova.php");
            }
        });
        llista_noms.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {


            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this,"Copiat" , Toast.LENGTH_SHORT).show();

                myClip = ClipData.newPlainText("text", list1.get(position));
                myClipboard.setPrimaryClip(myClip);
                return false;
            }


        });
        llista_noms.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:

                                        new CargarDatos().execute("http://riberdesigns.es/prova.php?id_nom="+list1.get(position));
                                        lv.clear();
                                        new Consultardatos().execute("http://riberdesigns.es/prova.php");

                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Estàs segur?").setPositiveButton("Si", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();


            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mymenu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.actual:
                        Uri uri = Uri.parse("https://dl.dropboxusercontent.com/u/29540046/app-release.apk"); // missing 'http://' will cause crashed
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private class CargarDatos extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), "Enviat", Toast.LENGTH_LONG).show();
            nom.setText("");
        }
    }

    private class Consultardatos extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {

                JSONArray ja = new JSONArray(result);

                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jsonc = ja.getJSONObject(i);
                    list1.add(jsonc.optString("nom"));

                }
                llista_noms.setAdapter(lv);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len =10000;

myurl = myurl.replace(" ","%20");
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("resposta", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            return readIt(is, len);

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public String readIt(InputStream stream, int len) throws IOException {
        Reader reader;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }
}
