package com.blablaarthur.lab2;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Артур on 04.10.2016.
 */

public class Notes extends AppCompatActivity {

    ListView notesListView;
    List<Note> notes = new ArrayList<Note>(0);

    int noteToDeleteEditId = 0;
    NoteAdapter notesAdapter = null;

    android.widget.SearchView search_view;
    MenuItem search_menu_item;
    private static final int CREATE_NOTE = 101;
    private static final int EDIT_NOTE = 102;
    private int FILTER = 3;
    private String searchText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes);
//        notes = new ArrayList<>(1);
//        notes.add(new Note(1, "Note 1", "Note 1 description", 1, Calendar.getInstance(), ""));
//        notes.add(new Note(2, "Note 2", "Note 2 description", 2, Calendar.getInstance(), ""));
//        notes.add(new Note(3, "Note 3", "Note 3 description", 0, Calendar.getInstance(), ""));
//        Calendar c = Calendar.getInstance();
//        c.set(2016, 9, 5, 12, 3);
//        notes.add(new Note(4, "Note 4", "Note 4 description", 2, c, ""));
//        notes.add(new Note(5, "Note 5", "Note 5 description", 1, Calendar.getInstance(), ""));


        notesAdapter = new NoteAdapter(this, notes);
        notesListView = (ListView) findViewById(R.id.notesListView);
        notesListView.setAdapter(notesAdapter);

        notesListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note current = notesAdapter.getItem(position);
                Intent myIntent = new Intent(Notes.this,
                        CreateNote.class);
                myIntent.setAction("android.intent.myaction.WATCH");
                myIntent.putExtra("Id", notesAdapter.getItem(position).Id);
                Log.d("A_R_T", notesAdapter.getItem(position).Id.toString() + " " + notesAdapter.getItem(position).Title.toString());
                startActivity(myIntent);
            }
        });

        registerForContextMenu(notesListView);

        ActionBar bar = getSupportActionBar();
        //bar.setLogo(R.drawable.ic_action_name);
        bar.setHomeButtonEnabled(true);
        bar.setDisplayUseLogoEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowHomeEnabled(true);

        getNotes();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        Log.d("A_R_T", info.position + "");
        noteToDeleteEditId = notesAdapter.getItem(info.position).Id;
        Log.d("A_R_T", noteToDeleteEditId + "");

        menu.add(0,0,0,R.string.edit);
        menu.add(0,1,1,R.string.delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Note current = notesAdapter.getItemById(noteToDeleteEditId);
        switch (item.getItemId()){
            case 0:

                Intent myIntent = new Intent(Notes.this,
                        CreateNote.class);
                myIntent.setAction("android.intent.myaction.EDIT");
                myIntent.putExtra("Id", current.Id);
                //search_view.setQuery("", false);
                //search_view.setIconified(true);
                startActivityForResult(myIntent, EDIT_NOTE);
                break;
            case 1:
                DBAdapter db = new DBAdapter(this);
                db.openDB();
                db.delete(noteToDeleteEditId);
                db.closeDB();
                //search_view.setQuery("", false);
                //search_view.setIconified(true);
                getNotes();
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflator = getMenuInflater();
        menuInflator.inflate(R.menu.notes_menu, menu);
        search_menu_item = menu.findItem(R.id.searchView);
        search_view = (android.widget.SearchView) search_menu_item.getActionView();
        search_view.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchText = newText;
                getNotes();
                return false;
            }
        });
        search_view.setOnCloseListener(new android.widget.SearchView.OnCloseListener(){
            @Override
            public boolean onClose() {
                searchText = null;
                getNotes();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.add_note_icon:
                Intent myIntent = new Intent(Notes.this,
                        CreateNote.class);
                myIntent.setAction("android.intent.myaction.CREATE");
                startActivityForResult(myIntent, CREATE_NOTE);
                break;
            case R.id.filter_notes:
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setTitle(R.string.importance);
                adb.setSingleChoiceItems(new CharSequence[]{getString(R.string.high), getString(R.string.medium) ,getString(R.string.low) ,getString(R.string.none)}, FILTER, selectImpFilter);
                adb.setPositiveButton(R.string.tofilter, selectImpFilter);
                adb.setNegativeButton(R.string.cancel, selectImpFilter);
                adb.create();
                adb.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    DialogInterface.OnClickListener selectImpFilter = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            ListView lv = ((AlertDialog) dialog).getListView();
            if(which == Dialog.BUTTON_POSITIVE){
                FILTER = lv.getCheckedItemPosition();
                getNotes();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CREATE_NOTE && resultCode == RESULT_OK){

        }
        else if(requestCode == EDIT_NOTE && resultCode == RESULT_OK){

        }
        getNotes();
    }

    public void getNotes(){
        notes.clear();
        DBAdapter db = new DBAdapter(this);
        db.openDB();
        Cursor c = db.retrieve(searchText, FILTER);
        while (c.moveToNext()) {
            Note n = new Note(c.getInt(0), c.getString(1), c.getString(2), c.getInt(3), c.getString(4), c.getString(5));
            notes.add(n);
        }
        db.closeDB();
        notesAdapter.notifyDataSetChanged();
    }
}
