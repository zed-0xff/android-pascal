// vim:ts=4:sw=4:expandtab
package me.zed_0xff.android.pascal;

import android.app.TabActivity;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ContextMenu;
import android.view.ContextMenu.*;
import android.view.View.OnCreateContextMenuListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.app.Dialog;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

import java.io.*;

import net.sourceforge.biff.Scanner;

import me.zed_0xff.android.pascal.R;

import com.admob.android.ads.AdManager;

public class Main extends Activity {

    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> mStrings = new ArrayList<String>();

	private String currentFilename, currentText, inputtedText;

	private static final int MENU_NEW    = Menu.FIRST;
	private static final int MENU_SAVE   = Menu.FIRST+1;
	private static final int MENU_RENAME = Menu.FIRST+2;
	private static final int MENU_DELETE = Menu.FIRST+3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //AdManager.setTestDevices( new String[] { AdManager.TEST_EMULATOR } );

        setContentView(R.layout.main);

        TabHost tabHost = (TabHost) this.findViewById(R.id.my_tabhost);
        tabHost.setup();

        ListView files_view = (ListView) this.findViewById(R.id.files);
        files_view.setOnItemClickListener(new OnItemClickListener(){
            public void onItemClick(AdapterView parent, View view, int position, long id){
                String fn = (String)parent.getItemAtPosition(position);
                load_file(fn);
            }
        });
        files_view.setOnCreateContextMenuListener(new OnCreateContextMenuListener(){
            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenuInfo menuInfo) {
              //super.onCreateContextMenu(menu, v, menuInfo);
              //menu.add(0, MENU_RENAME, 0, "Rename");
              menu.add(0, MENU_DELETE, 0, getResources().getText(R.string.delete));
            }
        });

	mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mStrings);
        files_view.setAdapter( mAdapter );

        tabHost.addTab(tabHost.newTabSpec("files")
                .setIndicator(getResources().getText(R.string.file))
                .setContent(R.id.files));
        tabHost.addTab(tabHost.newTabSpec("edit")
                .setIndicator(getResources().getText(R.string.edit))
                .setContent(R.id.edit));
        tabHost.addTab(tabHost.newTabSpec("run")
                .setIndicator(getResources().getText(R.string.run))
                .setContent(R.id.run));

        tabHost.setCurrentTabByTag("edit");

        tabHost.setOnTabChangedListener(new OnTabChangeListener(){
            @Override
            public void onTabChanged(String tabId) {
                android.view.View adView = findViewById( R.id.ad );

                if( tabId == "run" ) {
                    adView.setVisibility(android.view.View.VISIBLE);
                    runProgram();
                } else if( tabId == "files" ) {
                    adView.setVisibility(android.view.View.VISIBLE);
                    updateFilelist();
                } else {
                    // edit
                    adView.setVisibility(android.view.View.GONE);
                }
            }
        });
    }

    void updateFilelist(){
        String[] fnames = fileList();
        Arrays.sort(fnames);
        mAdapter.clear();
        for( String fname : fnames ) mAdapter.add( fname );
    }

    void runProgram(){
        TextView code= (TextView) findViewById( R.id.edit );
        TextView run = (TextView) findViewById( R.id.run  );
        String stdout = "", stderr = "";
        try {
            Scanner scanner = new Scanner(code.getText().toString(), this);
            stderr = scanner.stderr;
            stdout = scanner.stdout;
        } catch( Exception e ) {
            stderr = "EXCEPTION!\n" + e.toString() + "\n" + e.getStackTrace()[0].toString();
        }

        run.setText(stdout);

        if( stderr.length() != 0 ){
            run.append("\n" + stderr);
        }
    }

	public boolean onCreateOptionsMenu(Menu menu) {
	    menu.add(0, MENU_NEW,  0, getResources().getText(R.string.new_program)).
	    	setIcon(android.R.drawable.ic_menu_add);
	    menu.add(0, MENU_SAVE, 0, getResources().getText(R.string.save)).
	    	setIcon(android.R.drawable.ic_menu_save);
	    return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
            case MENU_NEW:
                newProgram();
                return true;
            case MENU_SAVE:
                save();
                return true;
	    }
	    return false;
	}

    public boolean onContextItemSelected(MenuItem item) {
      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
      switch (item.getItemId()) {
      case MENU_RENAME:
        return true;
      case MENU_DELETE:
        ListView files_view = (ListView) this.findViewById(R.id.files);
        String fn = (String)files_view.getItemAtPosition(info.position);
        deleteFile(fn);
        updateFilelist();
        return true;
      default:
        return super.onContextItemSelected(item);
      }
    }

	private static final int FILENAME_DIALOG = 0;
	private static final int    INPUT_DIALOG = 1;

    protected Dialog onCreateDialog(int id) {
        LayoutInflater factory = LayoutInflater.from(this);

        if( id == FILENAME_DIALOG ){
            final View filenameEntryView = factory.inflate(R.layout.dialog_filename, null);

            return new AlertDialog.Builder(this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(getResources().getText(R.string.filename))
                .setView(filenameEntryView)
                .setPositiveButton(getResources().getText(R.string.save), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* User clicked OK */
                        TextView t = (TextView) ((Dialog)dialog).findViewById(R.id.filename_edit);
                        currentFilename = t.getText().toString();
                        save_current_file();
                    }
                })
                .setNegativeButton(getResources().getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* User clicked cancel */
                    }
                })
                .create();
        } else {
            final View inputView = factory.inflate(R.layout.dialog_input, null);

            return new AlertDialog.Builder(this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(getResources().getText(R.string.input))
                .setView(inputView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* User clicked OK */
                        TextView t = (TextView) ((Dialog)dialog).findViewById(R.id.input_edit);
                        inputtedText = t.getText().toString();
                    }
                })
                .create();
        }
    }

    protected void onPrepareDialog(int id, Dialog d) {
        EditText t = (EditText) d.findViewById(R.id.filename_edit);

        if(currentFilename == null || currentFilename == ""){
            t.setText(guessProgramName());
        } else {
            t.setText(currentFilename);
        }
        t.selectAll();
    }

    String generateProgramName() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        Date date = new Date();
        return "p" + dateFormat.format(date);
    }

	void newProgram(){
        currentFilename = null;

        TabHost tabHost = (TabHost) this.findViewById(R.id.my_tabhost);
        tabHost.setCurrentTabByTag("edit");

        EditText t = (EditText) this.findViewById(R.id.edit);
        t.setText("program "+generateProgramName()+";\nbegin\n  \nend.");
        t.setSelection(31,31);
	}

    String guessProgramName(){
        TextView t = (TextView) this.findViewById(R.id.edit);
        String text = t.getText().toString();
        System.out.println(text);
        int pos = text.indexOf("program ");
        if(pos > -1 ){
            pos += 8;
            int len=0;
            char c = text.charAt(pos+len);
            while( (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') ){
                len++;
                c = text.charAt(pos+len);
            }
            if( len > 0 ){
                return text.substring(pos,pos+len);
            }
        }
        return generateProgramName();
    }

    void load_file( String fn ){
        currentFilename = null;
        try{
            FileInputStream f = openFileInput(fn);
            byte[] buffer = new byte[f.available()];
            f.read(buffer);
            f.close();
            String text = new String(buffer);
            TextView t = (TextView) this.findViewById(R.id.edit);
            t.setText(text);
            TabHost tabHost = (TabHost) this.findViewById(R.id.my_tabhost);
            tabHost.setCurrentTabByTag("edit");
            Toast.makeText(Main.this, fn + " loaded", Toast.LENGTH_SHORT).show();
            currentFilename = fn;
        } catch(IOException ex) {
            Toast.makeText(Main.this, "Error loading file!", Toast.LENGTH_SHORT).show();
        }
    }

    void save_current_file(){
        if(currentFilename != null && currentFilename != ""){
            String fn = currentFilename;
            if(!fn.endsWith(".pas")) fn += ".pas";
            try{
                TextView t = (TextView) this.findViewById(R.id.edit);
                FileOutputStream f = openFileOutput(fn, MODE_WORLD_READABLE);
                f.write(t.getText().toString().getBytes());
                f.close();
                Toast.makeText(Main.this, fn + " " + getResources().getText(R.string.saved), Toast.LENGTH_SHORT).show();
            } catch(IOException ex) {
                Toast.makeText(Main.this, "Error saving file!", Toast.LENGTH_SHORT).show();
            }
        }
    }

	void save(){
        if(currentFilename == null || currentFilename == ""){
            showDialog(FILENAME_DIALOG);
        } else {
            save_current_file();
        }
	}

    public String inputText(){
        showDialog(INPUT_DIALOG);
        return inputtedText;
    }
}

