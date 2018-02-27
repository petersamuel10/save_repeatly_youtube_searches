package com.example.jesus.twitter;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.FocusFinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences savedsearches;
    private TableLayout queryTableLayout;
    private EditText queryET;
    private EditText tagET;



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    //get the sharedpreferances that contains the user's saved searches
        savedsearches=getSharedPreferences("searches",MODE_PRIVATE);

        //get reference to the tableLayout
        queryTableLayout=(TableLayout) findViewById(R.id.queryTalbe);

        //get reference to the two editText and  the save button
        queryET = (EditText)findViewById(R.id.queryET);
        tagET=(EditText) findViewById(R.id.tagET);

        Button saveButton=(Button)findViewById(R.id.savebutton);
        saveButton.setOnClickListener(saveButtonClick);
        Button clearTags=(Button)findViewById(R.id.clearTags);
        clearTags.setOnClickListener(clearTagsButtons);

        refreshButton(null);
        queryTableLayout.requestFocus();


    }

         //recreate search tag and edit button for all saved searches
        // pass null to create all the tag and edit button
    private void refreshButton(String newTag) {

         //store saved tags in the tag array
        String[]tags=savedsearches.getAll().keySet().toArray(new String[0]);
        Arrays.sort(tags,String.CASE_INSENSITIVE_ORDER);

        if(newTag!=null)
            makeTagGUI(newTag,Arrays.binarySearch(tags,newTag));
        else
            for (int index=0;index<tags.length;++index)
            {
                makeTagGUI(tags[index],index);

            }
    }



    //add newTag button and corresponding  edit button to the GUI
    private void makeTagGUI(String tag ,int index)
    {
       //get referance to the LayoutInfliter services
        LayoutInflater infliter= (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        //inflater new_tag_View.xml file to create tag and edit button
        View newTagView = infliter.inflate(R.layout.new_tag_view,null);

        //get new tag button and register it's listener
        Button newTagButton =(Button)newTagView.findViewById(R.id.newTagbutton);

        newTagButton.setText(tag);
        newTagButton.setOnClickListener(queryButtonListener);

        //get new edit butoon and register it's listener
        Button newEditButton =(Button)newTagView.findViewById(R.id.newEditButton);
        newEditButton.setText("Edit");
        newEditButton.setOnClickListener(editButtonListener);

        Button delButton =(Button)newTagView.findViewById(R.id.delRow);
        delButton.setOnClickListener(delRow);

        //add new_tag and edit buttons to quaryTableLayout
        queryTableLayout.addView(newTagView,index);
    }


//add new search to the save file and then refresh button

    private void makeTagGUI(String query, String tag) {

        String originalQuery=savedsearches.getString(query,null);

        //get sharedpreferances.editor to store new tag/query pair
        SharedPreferences.Editor preferanceEditor=savedsearches.edit();
        preferanceEditor.putString(tag,query);
        preferanceEditor.apply();

        //if original is null then add tag in GUI
        if(originalQuery==null)
            refreshButton(tag);
    }

    private void clearButtons()
     {
        queryTableLayout.removeAllViews();
     }


     public View.OnClickListener saveButtonClick = new View.OnClickListener(){
         @Override
         public void onClick(View view) {

             //create tag if both edit texts aren't empty
             if(queryET.getText().length()>0&&tagET.getText().length()>0)
             {

                 makeTagGUI(queryET.getText().toString(),tagET.getText().toString());
                 queryET.setText("");
                 tagET.setText("");
                 //to hide soft keyboard
                 ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(tagET.getWindowToken(),0);

             }
             else
             {
                 //display message ask user to input quary and tag
                 AlertDialog.Builder builder =new AlertDialog.Builder(MainActivity.this);
                 builder.setTitle(R.string.missingTitle);
                 builder.setMessage(R.string.missingMessage);
                 builder.setPositiveButton(R.string.ok,null);
                 AlertDialog errorDialog =builder.create();
                 errorDialog.show();
             }
         }
     };


    public View.OnClickListener queryButtonListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {

            String buttonText=((Button)v).getText().toString();
            String query=savedsearches.getString(buttonText,null);
            String url=getString(R.string.searchURL)+query;

            // create an intent to open web browser
            Intent getURL=new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(getURL);
        }
    };



    public View.OnClickListener editButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            TableRow buttonTableRow = (TableRow) v.getParent();
            Button searchbutton = buttonTableRow.findViewById(R.id.newTagbutton);
            String tag = searchbutton.getText().toString();
            String query = savedsearches.getString(tag,null);
            tagET.setText(tag);
            queryET.setText(query);
            SharedPreferences.Editor preferanceEditor=savedsearches.edit();
            preferanceEditor.putString(tag,query);
            preferanceEditor.apply();

        }
    };
    public View.OnClickListener clearTagsButtons = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.confirmTitle);
            builder.setPositiveButton(R.string.earse, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    clearButtons();
                    SharedPreferences.Editor preferancesEditor=savedsearches.edit();
                    preferancesEditor.clear();
                    preferancesEditor.apply();

                }
            });
            builder.setCancelable(true);
            builder.setNegativeButton(R.string.cancel,null);//negative means doesn't matter
            builder.setMessage(R.string.confirmMessage);
            AlertDialog confirmDaialog = builder.create();
            confirmDaialog.show();
        }
    };


    public View.OnClickListener delRow = new View.OnClickListener() {

        @Override

        public void onClick(View v) {

           TableRow row= (TableRow) v.getParent();
            row.removeAllViews();
            Toast.makeText(getApplicationContext(),((Button)(row.getChildAt(0))).getText().toString(),Toast.LENGTH_LONG).show();

            //SharedPreferences.Editor preferancesEditor=savedsearches.edit();
            //preferancesEditor.remove(((Button)(row.getChildAt(0))).getText().toString());
            //preferancesEditor.apply();
        }

    };
}
