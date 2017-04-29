package com.lucasasselli.evernotewear;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.asyncclient.EvernoteNoteStoreClient;
import com.evernote.client.android.asyncclient.EvernoteUserStoreClient;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.User;
import com.evernote.thrift.TException;
import com.lucasasselli.common.ENContent;
import com.lucasasselli.common.database.NotebooksTable;
import com.lucasasselli.common.database.NotesTable;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class MyEvernote {

    private static final String TAG = "MyEvernote";

    public final static int OK = 0;
    public final static int ERROR_GENERIC = 1;
    public final static int ERROR_LOGIN = 2;

    // Uncharted settings
    public final static String NOTE_COUNT_KEY = "NOTE_COUNT_KEY";
    public final static String USERNAME_KEY = "USERNAME_KEY";
    public final static String LASTSYNC_KEY = "LASTSYNC_KEY";

    Context context;
    
    private EvernoteSession evernoteSession;

    private NotebooksTable notebooksTable;
    private NotesTable notesTable;
    private SharedPreferences sharedPreferences;
    private InternalData internalData;

    /**
     * This class is synchronous and must be used on a separate thread or in a service!
     */

    public MyEvernote(Context context){

        this.context = context;

        evernoteSession = new EvernoteSession.Builder(context)
                .setEvernoteService(Constants.EVERNOTE_SERVICE)
                .build(Constants.EVERNOTE_KEY, Constants.EVERNOTE_SECRET)
                .asSingleton();

        notebooksTable = new NotebooksTable(context);
        notesTable = new NotesTable(context);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        internalData = new InternalData(context);

    }

    public EvernoteSession getSession(){
        return evernoteSession;
    }

    public boolean isLoggedIn(){
        return evernoteSession.isLoggedIn();
    }

    public int addNote(String noteContent) {
        if (!isLoggedIn()) {
            Log.e(TAG, "Current session is not logged in");
            return ERROR_LOGIN;
        }

        Note note = new Note();

        // Retrieve title mode from settings
        int titleModePref = Integer.parseInt(sharedPreferences.getString(context.getString(R.string.pref_title_mode_key), context.getString(R.string.pref_title_mode_default)));
        String titlePref = sharedPreferences.getString(context.getString(R.string.pref_title_key), context.getString(R.string.pref_title_default));
        int noteCount = internalData.readInteger(NOTE_COUNT_KEY, 1);

        switch(titleModePref){
            case 0:
                note.setTitle(titlePref + " " + String.valueOf(noteCount));
                internalData.saveInteger(noteCount + 1, NOTE_COUNT_KEY);
                break;

            case 1:
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                note.setTitle(titlePref + " " + currentDateTimeString);
                break;
        }

        // Retrieve default notebook from settings
        String defaultNotebookPref = sharedPreferences.getString(context.getString(R.string.pref_default_notebook_key), null);
        if(defaultNotebookPref!=null){
            // Is possible that the selected notebook doesn't exist anymore: sync notebooks and search it
            Log.d(TAG, "Checking if default notebook is still valid");
            int notebookCheckResult = syncNotebooks();
            if(notebookCheckResult != OK){
                return notebookCheckResult;
            }
            // Find notebook in server
            if(notebooksTable.findGuid(defaultNotebookPref)>0){
                Log.d(TAG, "Default notebook found!");
                note.setNotebookGuid(defaultNotebookPref);
            }else{
                Log.d(TAG, "Default notebook not found! Clearing pref...");
                sharedPreferences.edit().putString(context.getString(R.string.pref_default_notebook_key), null).apply();
            }
        }
        
        note.setContent(ENContent.noteCreateBody(noteContent));

        EvernoteNoteStoreClient noteStoreClient = evernoteSession.getEvernoteClientFactory().getNoteStoreClient();

        try {
            noteStoreClient.createNote(note);
        } catch (EDAMUserException e) {
            Log.e(TAG, "User exception", e);
            return ERROR_GENERIC;
        } catch (EDAMSystemException e) {
            Log.e(TAG, "System exception", e);
            return ERROR_GENERIC;
        } catch (EDAMNotFoundException e) {
            Log.e(TAG, "EDAM not found", e);
            return ERROR_GENERIC;
        } catch (TException e) {
            Log.e(TAG, "Unknown", e);
            return ERROR_GENERIC;
        }

        return OK;
    }

    public int deleteNote(String noteGuid) {
        if (!isLoggedIn()) {
            Log.e(TAG, "Current session is not logged in");
            return ERROR_LOGIN;
        }

        EvernoteNoteStoreClient noteStoreClient = evernoteSession.getEvernoteClientFactory().getNoteStoreClient();

        try {
            noteStoreClient.deleteNote(noteGuid);
        } catch (EDAMUserException e) {
            Log.e(TAG, "User exception", e);
            return ERROR_GENERIC;
        } catch (EDAMSystemException e) {
            Log.e(TAG, "System exception", e);
            return ERROR_GENERIC;
        } catch (EDAMNotFoundException e) {
            Log.e(TAG, "EDAM not found", e);
            return ERROR_GENERIC;
        } catch (TException e) {
            Log.e(TAG, "Unknown", e);
            return ERROR_GENERIC;
        }

        return OK;
    }

    private Note getNote(EvernoteNoteStoreClient noteStoreClient, String guid){
        Note note = null;

        try {
            note = noteStoreClient.getNote(guid, true, false, false, false);
        } catch (EDAMUserException e) {
            Log.e(TAG, "User exception", e);
        } catch (EDAMSystemException e) {
            Log.e(TAG, "System exception", e);
        } catch (EDAMNotFoundException e) {
            Log.e(TAG, "EDAM not found", e);
        } catch (TException e) {
            Log.e(TAG, "Unknown", e);
        }

        return note;
    }

    public int syncNotebooks(){
        if (!isLoggedIn()) {
            Log.e(TAG, "Current session is not logged in");
            return ERROR_LOGIN;
        }

        EvernoteNoteStoreClient noteStoreClient = evernoteSession.getEvernoteClientFactory().getNoteStoreClient();

        List<Notebook> remoteNotebooks;

        Log.d(TAG, "Updating notebooks..." );

        try {
            remoteNotebooks = noteStoreClient.listNotebooks();
        } catch (EDAMUserException e) {
            Log.e(TAG, "User exception", e);
            return ERROR_GENERIC;
        } catch (EDAMSystemException e) {
            Log.e(TAG, "System exception", e);
            return ERROR_GENERIC;
        } catch (TException e) {
            Log.e(TAG, "Unknown", e);
            return ERROR_GENERIC;
        }

        // Update local content
        for (Notebook notebook : remoteNotebooks) {
            String guid = notebook.getGuid();
            int guidCount = notebooksTable.findGuid(guid);
            if(guidCount == 0){
                // Guid doesn't exist, add new entry
                notebooksTable.add(notebook);
                Log.d(TAG, "Added notebook GUID " + guid);
            }else if(guidCount == 1){
                // Guid exist, check if has been updated
                Notebook oldNotebook = notebooksTable.getNotebook(guid);
                if(oldNotebook.getUpdateSequenceNum()<notebook.getUpdateSequenceNum()){
                    // Update notebook
                    Log.d(TAG, "Notebook GUID " + guid + " must be updated, old USN is" + oldNotebook.getUpdateSequenceNum() + " and new is " + notebook.getUpdateSequenceNum());
                    notebooksTable.update(guid, notebook);
                }
            }else{
                Log.e(TAG, "Search for notebook with GUID " + guid + " returned " + guidCount);
                return ERROR_GENERIC;
            }
        }

        // Check deleted notebooks
        List<Notebook> localNotebooks = notebooksTable.fetchNotebooks();
        if(remoteNotebooks.size()!=localNotebooks.size()) {
            for (Notebook loacalNotebook : localNotebooks) {
                // Search this notebook
                boolean found = false;
                for (Notebook remoteNotebook : remoteNotebooks) {
                    if(remoteNotebook.getGuid().equals(loacalNotebook.getGuid())){
                        // Notebook found!
                        found = true;
                        break;
                    }
                }
                if(!found){
                    Log.d(TAG, "Notebook GUID " + loacalNotebook.getGuid() + " not found: must be deleted");
                    notebooksTable.deleteNotebook(loacalNotebook.getGuid());
                }
            }
        }

        return OK;
    }


    public int syncNotes(){
        if (!isLoggedIn()) {
            Log.e(TAG, "Current session is not logged in");
            return ERROR_LOGIN;
        }

        EvernoteNoteStoreClient noteStoreClient = evernoteSession.getEvernoteClientFactory().getNoteStoreClient();

        List<Note> remoteNotes;

        // Create the search filter
        NoteFilter filter = new NoteFilter();
        filter.setOrder(NoteSortOrder.CREATED.getValue());
        filter.setAscending(true);

        Log.d(TAG, "Updating notes..." );

        // Retrieve sync amount from settings
        int sync_amount = Integer.parseInt(sharedPreferences.getString(context.getString(R.string.pref_sync_amount_key), context.getString(R.string.pref_sync_amount_default)));

        try {
            remoteNotes = noteStoreClient.findNotes(filter, 0, sync_amount).getNotes();
        } catch (EDAMUserException e) {
            Log.e(TAG, "User exception", e);
            return ERROR_GENERIC;
        } catch (EDAMSystemException e) {
            Log.e(TAG, "System exception", e);
            return ERROR_GENERIC;
        } catch (EDAMNotFoundException e) {
            Log.e(TAG, "EDAM not found", e);
            return ERROR_GENERIC;
        } catch (TException e) {
            Log.e(TAG, "Unknown", e);
            return ERROR_GENERIC;
        }

        // Update local content
        for (Note note : remoteNotes) {
            String guid = note.getGuid();
            int guidCount = notesTable.findGuid(guid);
            if(guidCount == 0){
                // Guid doesn't exist, add new entry.
                // Get note remote content
                Note fullNote = getNote(noteStoreClient, guid);
                if(fullNote!=null){
                    notesTable.add(fullNote);
                    Log.d(TAG, "Added note GUID " + guid);
                }
            }else if(guidCount == 1){
                // Guid exist, check if has been updated
                Note oldNote = notesTable.getNote(guid);
                if(oldNote.getUpdateSequenceNum()<note.getUpdateSequenceNum()){
                    // Update notebook
                    // Get note remote content
                    Note fullNote = getNote(noteStoreClient, guid);
                    if(fullNote!=null){
                        notesTable.update(guid, fullNote);
                        Log.d(TAG, "Note GUID " + guid + " must be updated, old USN is " + oldNote.getUpdateSequenceNum() + " and new is " + note.getUpdateSequenceNum());
                    }
                }
            }else{
                Log.e(TAG, "Search for note with GUID " + guid + " returned " + guidCount);
                return ERROR_GENERIC;
            }
        }

        // Check deleted notebooks
        List<Note> localNotes = notesTable.fetchNotes();
        if(remoteNotes.size()!=localNotes.size()) {
            for (Note loacalNote : localNotes) {
                // Search this notebook
                boolean found = false;
                for (Note remoteNote : remoteNotes) {
                    if(remoteNote.getGuid().equals(loacalNote.getGuid())){
                        // Notebook found!
                        found = true;
                        break;
                    }
                }
                if(!found){
                    Log.d(TAG, "Note GUID " + loacalNote.getGuid() + " not found: must be deleted");
                    notesTable.deleteNote(loacalNote.getGuid());
                }
            }
        }

        return OK;
    }

    public int syncUserInfo(){
        if (!isLoggedIn()) {
            Log.e(TAG, "Current session is not logged in");
            return ERROR_LOGIN;
        }

        EvernoteUserStoreClient userStoreClient = evernoteSession.getEvernoteClientFactory().getUserStoreClient();

        User user;

        Log.d(TAG, "Updating user info..." );

        try {
            user = userStoreClient.getUser();
        } catch (EDAMUserException e) {
            Log.e(TAG, "User exception", e);
            return ERROR_GENERIC;
        } catch (EDAMSystemException e) {
            Log.e(TAG, "System exception", e);
            return ERROR_GENERIC;
        } catch (TException e) {
            Log.e(TAG, "Unknown", e);
            return ERROR_GENERIC;
        }

        // TODO Check if null
        internalData.saveString(user.getUsername(), USERNAME_KEY);

        return OK;
    }

    public int syncAll(){
        // Sync notebooks
        int notebooksSyncResult = syncNotebooks();
        if(notebooksSyncResult != OK){
            return notebooksSyncResult;
        }

        // Sync notes
        int notesSyncResult = syncNotes();
        if(notesSyncResult != OK){
            return notesSyncResult;
        }

        // Sync notes
        int userSyncResult = syncUserInfo();
        if(userSyncResult != OK){
            return userSyncResult;
        }

        // Update last sync date
        long currentTimestamp = System.currentTimeMillis()/1000;
        internalData.saveLong(currentTimestamp, LASTSYNC_KEY);

        return OK;
    }
}
