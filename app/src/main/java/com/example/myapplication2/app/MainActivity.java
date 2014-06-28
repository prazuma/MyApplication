package com.example.myapplication2.app;

import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.ArrayList;
import android.net.Uri;
import android.database.Cursor;
import android.provider.ContactsContract;



public class MainActivity extends ActionBarActivity{
    public final static String EXTRA_MESSAGE="com.example.myapplication2.MESSAGE";
    public static int REQUEST_SPEECH=1;
    public static String search="http://www.google.co.jp/search?";
    private static final int REQUEST_IMAGE_CAPTURE=0;
    private ImageView imageview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

    }


    public void sendMessage(View view){
        Intent intent=new Intent(this,MainActivity2.class);
        EditText editText=(EditText)findViewById(R.id.edit_message);
        String message=editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE,message);
        startActivity(intent);
    }

    public void sendVoice(View view){
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "どうぞ");
            startActivityForResult(intent, REQUEST_SPEECH);
        }catch(ActivityNotFoundException e){
            Toast.makeText(this,"Error!!You Can't",Toast.LENGTH_LONG).show();
        }

    }

    private String getContactPhone(String n){//電話番号を返す
        ContentResolver resolver=getContentResolver();
        Cursor cursor=resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);
        String name="";
        String phone="";
        int indexName=cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        if(cursor.moveToFirst()){
            while(true){
                if(n.equals(cursor.getString(indexName))){
                    int indexPhone=cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA1);
                    phone=cursor.getString(indexPhone);
                    name=n;
                }
                if(!cursor.moveToNext())break;
            }
        }
        return phone;//nさんが連絡先に入ってなければ、空白のまま
    }

    private String getContactEmail(String n){//メールアドレスを返す
        ContentResolver resolver=getContentResolver();
        Cursor cursor=resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,null,null,null,null);
        String name="";
        String email="";
        int indexName=cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME);
        if(cursor.moveToFirst()){
            while(true){
                if(n.equals(cursor.getString(indexName))){
                    int indexEmail=cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA1);
                    email=cursor.getString(indexEmail);
                    name=n;
                }
                if(!cursor.moveToNext())break;
            }
        }
        return email;//nさんが連絡先に入ってなければ、空白のまま
    }

    private void CallSomeone(String phone){
        TelephonyManager telephonyManager=(TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        String tell="tel:"+phone;
    }

    private boolean[] setFlag(String s){
        boolean[] f=new boolean[4];
        if(s.contains("検索"))f[0]=true;
        if(s.contains("電話"))f[1]=true;
        if(s.contains("メール"))f[2]=true;
        if(s.contains("カメラ"))f[3]=true;
        return f;
    }


    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode==REQUEST_SPEECH && resultCode==RESULT_OK){
            boolean[]flag=new boolean[4];
            ArrayList<String>set=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            flag=setFlag(set.get(0));
            Intent intent=new Intent();
            if(flag[1]){
                String phone="tel:";
                if(set.get(0).length()!=2){
                    String name=set.get(0).substring(0,set.get(0).length()-4);
                    phone += getContactPhone(name);
                }
                TelephonyManager telephonyManager=(TelephonyManager)getSystemService(TELEPHONY_SERVICE);
                if(phone.equals("tel:"))intent.setAction(Intent.ACTION_DIAL);
                else intent.setAction(Intent.ACTION_CALL);
                intent.setData(Uri.parse(phone));
                startActivity(intent);
            }

            else if(flag[0]){
                if(set.get(0).length()!=2) {
                    String keyword = set.get(0).substring(0, set.get(0).length() - 4);
                    search += "q=" + keyword + "&oq=" + keyword;
                }
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(search));
                startActivity(intent);
            }

            else if(flag[2]){

                Intent mail=new Intent(this,MailActivity.class);
                startActivity(mail);

/*
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/Gmail");
                startActivity(intent);
                */
            }

            else if(flag[3]){
                intent.setAction("android.media.action.IMAGE_CAPTURE");
                startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);
            }

            else {
                Toast toast=Toast.makeText(this,set.get(0),Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }

        }

        if(requestCode==REQUEST_IMAGE_CAPTURE && resultCode==RESULT_OK){
            try{
                Bitmap image=(Bitmap)data.getExtras().get("data");
                imageview.setImageBitmap(image);
            }catch (Exception e){

            }
        }

        super.onActivityResult(requestCode,resultCode,data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_camera:
                Intent intent=new Intent();
                intent.setAction("android.media.action.IMAGE_CAPTURE");
                startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
}
