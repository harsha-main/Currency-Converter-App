package com.reallycool.harsha.convertercurrency;

import android.app.Activity;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener {
    static String f = "";
    static String t = "";
    static TextView answer;
    static EditText edit;
    int first = 0, second = 0;
    int favorite = -1;
    String favstring;
    CheckBox def;
    double[] values;
    CheckBox fav;
    TextView tv2;
    double currency;
    SharedPreferences sh;
    SharedPreferences.Editor editor;
    Spinner to;
    boolean ready = false;
    Spinner from;
    Button but;
    ListView lv;
    ArrayList<Integer> al;
    String ans = "";
    String cou[];
    ConnectivityManager cm = null;
    String tra = "";

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        def = (CheckBox) findViewById(R.id.checkBox);
        lv = (ListView) findViewById(R.id.listview);
        fav = (CheckBox) findViewById(R.id.checkBox2);
        sh = getApplicationContext().getSharedPreferences("Exchanges", 0);
        if (!isNetworkConnected()&&sh.getInt("First",0)<1) {
            Toast.makeText(this, "Check your Internet connection and try again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tv2 = (TextView) findViewById(R.id.tv2);
        from = (Spinner) findViewById(R.id.from);
        to = (Spinner) findViewById(R.id.to);
        cou = getResources().getStringArray(R.array.countries);
        al = new ArrayList<>();
        answer = (TextView) findViewById(R.id.answer);
        but = (Button) findViewById(R.id.button);
        edit = (EditText) findViewById(R.id.edit);
        edit.setText("1");
        favorite = sh.getInt("def", -1);
        favstring = sh.getString("favstring", "");
        Log.e(favstring, "Takealook");
        if (!favstring.equals("")) {
            String s[] = favstring.split(" ");
            for (String string : s) {
                al.add(Integer.parseInt(string));
            }
        }

        answer.setText("value");
        but.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!ready) return false;
                to.requestFocus();
                hideSoftKeyboard(MainActivity.this);
                String s = edit.getText().toString();
                currency = Double.parseDouble(s);
                if (currency == 0 || s == "0" || s == "00") {
                    Toast.makeText(MainActivity.this, "Enter a value  first", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (first == second) {
                    Toast.makeText(MainActivity.this, "Choose currencies", Toast.LENGTH_SHORT).show();
                    return true;
                }
                show();
                return true;
            }
        });

// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.countries, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        from.setAdapter(adapter);
        from.setOnItemSelectedListener(this);
        if (favorite > -1) {
            from.setSelection(favorite);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        to.setAdapter(adapter);
        to.setOnItemSelectedListener(this);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!isNetworkConnected()&&sh.getInt("First",0)>0) {
            Toast.makeText(this, "Offline Mode", Toast.LENGTH_SHORT).show();
            retrieve();
            return;
        }
        calculate();
    }

    private void show() {
        currency = currency / values[first];
        double t = currency;
        currency = currency * values[second];
        String ts = Double.toString(currency);
        if (ts.length() > 11)
            ts = ts.substring(0, ts.length() - 11) + " ";
        answer.setText(ts);
        ArrayList<String> as = new ArrayList<String>();
        for (int i = 0; i < al.size(); i++) {
            String str = Double.toString(t * values[al.get(i)]);
            Log.e("String", str);
            if (str.length() > 11)
                str = str.substring(0, str.length() - 11);
            as.add(cou[al.get(i)] + " " + str);
        }
        Log.e(as.size() + "", "Sizes");
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.list_text, as);
        lv.setAdapter(adapter);
    }

    public void clicked(View view) {
        editor = sh.edit();
        boolean check = ((CheckBox) view).isChecked();
        favorite = from.getSelectedItemPosition();
        if (check)
            editor.putInt("def", favorite);
        else
            editor.putInt("def", -1);
        editor.commit();
    }

    public void cli(View view) {
        editor = sh.edit();
        boolean check = ((CheckBox) view).isChecked();
        if (check) {
            al.add((to.getSelectedItemPosition()));
            String str = "";
            for (Integer i = 0; i < al.size(); i++) {
                str += al.get(i) + " ";
            }
            editor.putString("favstring", str);
        } else {
            if (al.contains(to.getSelectedItemPosition())) {
                al.remove(al.indexOf(to.getSelectedItemPosition()));
            }
            String str = "";
            for (Integer i = 0; i < al.size(); i++) {
                str += al.get(i).toString() + " ";
            }
            Log.e("favstring", str);
            editor.putString("favstring", str);
        }
        boolean boo = editor.commit();
        Log.e("commit", boo + "see");

        Toast.makeText(this, al.size() + "", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (parent.getId() == R.id.from) {
            first = position;
            f = (String) parent.getItemAtPosition(position);
            if (position == favorite) def.setChecked(true);
            else def.setChecked(false);
        }

        if (parent.getId() == R.id.to) {
            second = position;
            t = (String) parent.getItemAtPosition(position);
            boolean b = false;
            for (int i = 0; i < al.size(); i++) {
                if (position == al.get(i)) {
                    b = true;
                    break;
                }
            }
            if (b) fav.setChecked(true);
            else fav.setChecked(false);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    synchronized void calculate() {
        tra = "";
        new AsyncTask<URL, Integer, Long>() {

            @Override
            protected Long doInBackground(URL... params) {

                String str = "http://api.fixer.io/latest";
                URL url = null;
                try {
                    url = new URL(str);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                BufferedReader in = null;
                try {
                    in = new BufferedReader(
                            new InputStreamReader(url.openStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String inputLine = null;
                Log.e("something", "someone");
                try {
                    while ((inputLine = in.readLine()) != null) {
                        tra += inputLine;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Long result) {

                ans = tra.split("rates")[1];
                ans = ans.substring(3, ans.length() - 2);
                process();

                Calendar c = Calendar.getInstance();
                editor= sh.edit();
                editor.putString("ExcValues", ans);
                editor.putInt("First", 1);
                editor.putString("Time", " " + c.getTime());
                editor.commit();
            }
        }.execute();

    }

    private void process() {

        String[] v = ans.split(",");
        int le = v.length;
        values = new double[le];
        for (int i = 0; i < le; i++) {
            values[i] = Double.parseDouble(v[i].substring(6, v[i].length()));
        }
        ready = true;
    }
//For offline mode

    private void retrieve() {
         ans=sh.getString("ExcValues",null);
         process();
         tv2.setText("Last updated on:"+sh.getString("Time","")+"  The rates are updated daily around 4PM CET.");

    }

}
