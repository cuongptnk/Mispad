package com.mythopc.nova.mispad;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.mythopc.nova.mispad.soundfile.CheapSoundFile;

import java.io.File;
import java.util.ArrayList;

/**
 * Main screen that shows up when you launch Ringdroid.  Handles selecting
 * an audio file or using an intent to record a new one, and then
 * launches RingdroidEditActivity from here.
 */
public class MainActivity
        extends ListActivity
        implements TextWatcher
{
    private TextView mFilter;
    private SimpleCursorAdapter mAdapter;
    private boolean mWasGetContentIntent;
    private boolean mShowAll;

    // Result codes
    private static final int REQUEST_CODE_EDIT = 1;
    private static final int REQUEST_CODE_CHOOSE_CONTACT = 2;

    // Menu commands
    private static final int CMD_ABOUT = 1;
    private static final int CMD_PRIVACY = 2;
    private static final int CMD_SHOW_ALL = 3;

    // Context menu
    private static final int CMD_EDIT = 4;
    private static final int CMD_DELETE = 5;
    private static final int CMD_SET_AS_DEFAULT = 6;
    private static final int CMD_SET_AS_CONTACT = 7;
    String filename,countname;

    private static int count = 0;
    private String file1, file2;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mShowAll = false;

        countname=getIntent().getStringExtra("Select2");

        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            showFinalAlert(getResources().getText(R.string.sdcard_readonly));
            return;
        }
        if (status.equals(Environment.MEDIA_SHARED)) {
            showFinalAlert(getResources().getText(R.string.sdcard_shared));
            return;
        }
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            showFinalAlert(getResources().getText(R.string.no_sdcard));
            return;
        }


        // Inflate our UI from its XML layout description.
        setContentView(R.layout.media_select);




        try {
            mAdapter = new SimpleCursorAdapter(
                    this,
                    // Use a template that displays a text view
                    R.layout.media_select_row,
                    // Give the cursor to the list adatper
                    createCursor(""),
                    // Map from database columns...
                    new String[] {
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.ALBUM,
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media._ID},
                    // To widget ids in the row layout...
                    new int[] {
                            R.id.row_artist,
                            R.id.row_album,
                            R.id.row_title,
                            R.id.row_icon,
                    },1);

            setListAdapter(mAdapter);

            getListView().setItemsCanFocus(true);

            // Normal click - open the editor
            getListView().setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView parent,
                                        View view,
                                        int position,
                                        long id) {

                    Cursor c = mAdapter.getCursor();
                    int title = c.getColumnIndex(MediaStore.Audio.Media.DATA);
                    String filename=c.getString(title);

                    if (count == 0) {
                        file1 = filename;
                        count++;
                        Toast.makeText(getApplicationContext(), "Select 1 more file", Toast.LENGTH_LONG).show();
                    } else if (count == 1) {
                        file2 = filename;
                        count++;
                        Intent intent = new Intent(getApplicationContext(), DJPad.class);
                        intent.putExtra("filename1", file1);
                        intent.putExtra("filename2", file2);
                        finish();
                        startActivity(intent);
                    }

                    //	Intent intent = new Intent(Intent.ACTION_EDIT,Uri.parse(filename));
                    //    intent.setClassName("com.spkd.mp3editor","com.spkd.mp3editor.RingEditActivity");
                    //   startActivityForResult(intent,0);
                    //Intent intent=new Intent(getApplicationContext(), FileEdit.class);
                    //intent.putExtra("filename",filename);
                    //finish();
                    //startActivity(intent);

                }
            });

        } catch (SecurityException e) {
            // No permission to retrieve audio?

            // todo error 1
        } catch (IllegalArgumentException e) {
            // No permission to retrieve audio?

            // todo error 2
        }

        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            public boolean setViewValue(View view,
                                        Cursor cursor,
                                        int columnIndex) {


                return false;
            }
        });

        // Long-press opens a context menu
        registerForContextMenu(getListView());

        mFilter = (TextView) findViewById(R.id.search_filter);
        if (mFilter != null) {
            mFilter.addTextChangedListener(this);
        }
    }



    /** Called with an Activity we started with an Intent returns. */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent dataIntent) {
        if (requestCode != REQUEST_CODE_EDIT) {
            return;
        }

        if (resultCode != RESULT_OK) {
            return;
        }

        setResult(RESULT_OK, dataIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(CMD_ABOUT).setVisible(true);
        menu.findItem(CMD_PRIVACY).setVisible(true);
        menu.findItem(CMD_SHOW_ALL).setVisible(true);
        menu.findItem(CMD_SHOW_ALL).setEnabled(!mShowAll);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CMD_ABOUT:
                //   RingdroidEditActivity.onAbout(this);
                return true;
            case CMD_PRIVACY:
                showPrivacyDialog();
                return true;
            case CMD_SHOW_ALL:
                mShowAll = true;
                refreshListView();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu,
                                    View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        Cursor c = mAdapter.getCursor();
        String title = c.getString(c.getColumnIndexOrThrow(
                MediaStore.Audio.Media.TITLE));

        menu.setHeaderTitle(title);

        menu.add(0, CMD_EDIT, 0, R.string.context_menu_edit);
        menu.add(0, CMD_DELETE, 0, R.string.context_menu_delete);

        // Add items to the context menu item based on file type
        if (0 != c.getInt(c.getColumnIndexOrThrow(
                MediaStore.Audio.Media.IS_RINGTONE))) {
            menu.add(0, CMD_SET_AS_DEFAULT, 0, R.string.context_menu_default_ringtone);
            menu.add(0, CMD_SET_AS_CONTACT, 0, R.string.context_menu_contact);
        } else if (0 != c.getInt(c.getColumnIndexOrThrow(
                MediaStore.Audio.Media.IS_NOTIFICATION))) {
            menu.add(0, CMD_SET_AS_DEFAULT, 0, R.string.context_menu_default_notification);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        @SuppressWarnings("unused")
        AdapterContextMenuInfo info =
                (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case CMD_EDIT:
                startRingdroidEditor();
                return true;
            case CMD_DELETE:
                confirmDelete();
                return true;
            case CMD_SET_AS_DEFAULT:
                setAsDefaultRingtoneOrNotification();
                return true;
            case CMD_SET_AS_CONTACT:
                return chooseContactForRingtone(item);
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void showPrivacyDialog() {
        try {
            Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse(""));
            intent.putExtra("privacy", true);
            intent.setClassName("com.ringdroid",
                    "com.ringdroid.RingdroidEditActivity");
            startActivityForResult(intent, REQUEST_CODE_EDIT);
        } catch (Exception e) {
            Log.e("Ringdroid", "Couldn't show privacy dialog");
        }
    }

    private void setAsDefaultRingtoneOrNotification(){
        Cursor c = mAdapter.getCursor();

        // If the item is a ringtone then set the default ringtone,
        // otherwise it has to be a notification so set the default notification sound
        if (0 != c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_RINGTONE))){
            RingtoneManager.setActualDefaultRingtoneUri(
                    MainActivity.this,
                    RingtoneManager.TYPE_RINGTONE,
                    getUri());
            Toast.makeText(
                    MainActivity.this,
                    R.string.default_ringtone_success_message,
                    Toast.LENGTH_SHORT)
                    .show();
        } else {
            RingtoneManager.setActualDefaultRingtoneUri(
                    MainActivity.this,
                    RingtoneManager.TYPE_NOTIFICATION,
                    getUri());
            Toast.makeText(
                    MainActivity.this,
                    R.string.default_notification_success_message,
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private Uri getUri(){
        //Get the uri of the item that is in the row
        Cursor c = mAdapter.getCursor();
        int uriIndex = c.getColumnIndex(
                "\"" + MediaStore.Audio.Media.INTERNAL_CONTENT_URI + "\"");
        if (uriIndex == -1) {
            uriIndex = c.getColumnIndex(
                    "\"" + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "\"");
        }
        String itemUri = c.getString(uriIndex) + "/" +
                c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
        return (Uri.parse(itemUri));
    }

    private boolean chooseContactForRingtone(MenuItem item){
        try {
            //Go to the choose contact activity
            Intent intent = new Intent(Intent.ACTION_EDIT, getUri());
            intent.setClassName(
                    "com.ringdroid",
                    "com.ringdroid.ChooseContactActivity");
            startActivityForResult(intent, REQUEST_CODE_CHOOSE_CONTACT);
        } catch (Exception e) {
            Log.e("Ringdroid", "Couldn't open Choose Contact window");
        }
        return true;
    }

    private void confirmDelete() {
        // See if the selected list item was created by Ringdroid to
        // determine which alert message to show
        Cursor c = mAdapter.getCursor();
        int artistIndex = c.getColumnIndexOrThrow(
                MediaStore.Audio.Media.ARTIST);
        String artist = c.getString(c.getColumnIndexOrThrow(
                MediaStore.Audio.Media.ARTIST));
        CharSequence ringdroidArtist =
                getResources().getText(R.string.artist_name);

        CharSequence message;
        if (artist.equals(ringdroidArtist)) {
            message = getResources().getText(
                    R.string.confirm_delete_ringdroid);
        } else {
            message = getResources().getText(
                    R.string.confirm_delete_non_ringdroid);
        }

        CharSequence title;
        if (0 != c.getInt(c.getColumnIndexOrThrow(
                MediaStore.Audio.Media.IS_RINGTONE))) {
            title = getResources().getText(R.string.delete_ringtone);
        } else if (0 != c.getInt(c.getColumnIndexOrThrow(
                MediaStore.Audio.Media.IS_ALARM))) {
            title = getResources().getText(R.string.delete_alarm);
        } else if (0 != c.getInt(c.getColumnIndexOrThrow(
                MediaStore.Audio.Media.IS_NOTIFICATION))) {
            title = getResources().getText(R.string.delete_notification);
        } else if (0 != c.getInt(c.getColumnIndexOrThrow(
                MediaStore.Audio.Media.IS_MUSIC))) {
            title = getResources().getText(R.string.delete_music);
        } else {
            title = getResources().getText(R.string.delete_audio);
        }

        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                        R.string.delete_ok_button,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                onDelete();
                            }
                        })
                .setNegativeButton(
                        R.string.delete_cancel_button,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                            }
                        })
                .setCancelable(true)
                .show();
    }

    private void onDelete() {
        Cursor c = mAdapter.getCursor();
        int dataIndex = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        String filename = c.getString(dataIndex);

        int uriIndex = c.getColumnIndex(
                "\"" + MediaStore.Audio.Media.INTERNAL_CONTENT_URI + "\"");
        if (uriIndex == -1) {
            uriIndex = c.getColumnIndex(
                    "\"" + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "\"");
        }
        if (uriIndex == -1) {
            showFinalAlert(getResources().getText(R.string.delete_failed));
            return;
        }

        if (!new File(filename).delete()) {
            showFinalAlert(getResources().getText(R.string.delete_failed));
        }

        String itemUri = c.getString(uriIndex) + "/" +
                c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
        getContentResolver().delete(Uri.parse(itemUri), null, null);
    }

    private void showFinalAlert(CharSequence message) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(getResources().getText(R.string.alert_title_failure))
                .setMessage(message)
                .setPositiveButton(
                        R.string.alert_ok_button,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                finish();
                            }
                        })
                .setCancelable(false)
                .show();
    }



    private void startRingdroidEditor() {
        Cursor c = mAdapter.getCursor();
        int dataIndex = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        String filename = c.getString(dataIndex);
        try {
            Intent intent = new Intent(Intent.ACTION_EDIT,
                    Uri.parse(filename));
            intent.putExtra("was_get_content_intent",
                    mWasGetContentIntent);
            intent.setClassName(
                    "com.ringdroid",
                    "com.ringdroid.RingdroidEditActivity");
            startActivityForResult(intent, REQUEST_CODE_EDIT);
        } catch (Exception e) {
            Log.e("Ringdroid", "Couldn't start editor");
        }
    }

    private Cursor getInternalAudioCursor(String selection,
                                          String[] selectionArgs) {
        return managedQuery(
                MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
                INTERNAL_COLUMNS,
                selection,
                selectionArgs,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
    }

    private Cursor getExternalAudioCursor(String selection,
                                          String[] selectionArgs) {
        return managedQuery(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                EXTERNAL_COLUMNS,
                selection,
                selectionArgs,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
    }

    Cursor createCursor(String filter) {
        ArrayList<String> args = new ArrayList<String>();
        String selection;

        if (mShowAll) {
            selection = "(_DATA LIKE ?)";
            args.add("%");
        } else {
            selection = "(";
            for (String extension : CheapSoundFile.getSupportedExtensions()) {
                args.add("%." + extension);
                if (selection.length() > 1) {
                    selection += " OR ";
                }
                selection += "(_DATA LIKE ?)";
            }
            selection += ")";

            selection = "(" + selection + ") AND (_DATA NOT LIKE ?)";
            args.add("%espeak-data/scratch%");
        }

        if (filter != null && filter.length() > 0) {
            filter = "%" + filter + "%";
            selection =
                    "(" + selection + " AND " +
                            "((TITLE LIKE ?) OR (ARTIST LIKE ?) OR (ALBUM LIKE ?)))";
            args.add(filter);
            args.add(filter);
            args.add(filter);
        }

        String[] argsArray = args.toArray(new String[args.size()]);

        Cursor external = getExternalAudioCursor(selection, argsArray);
        Cursor internal = getInternalAudioCursor(selection, argsArray);

        Cursor c = new MergeCursor(new Cursor[] {
                getExternalAudioCursor(selection, argsArray),
                getInternalAudioCursor(selection, argsArray)});
        startManagingCursor(c);
        return c;
    }

    public void beforeTextChanged(CharSequence s, int start,
                                  int count, int after) {
    }

    public void onTextChanged(CharSequence s,
                              int start, int before, int count) {
    }

    public void afterTextChanged(Editable s) {
        refreshListView();
    }

    private void refreshListView() {
        String filterStr = mFilter.getText().toString();
        mAdapter.changeCursor(createCursor(filterStr));
    }

    private static final String[] INTERNAL_COLUMNS = new String[] {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.IS_RINGTONE,
            MediaStore.Audio.Media.IS_ALARM,
            MediaStore.Audio.Media.IS_NOTIFICATION,
            MediaStore.Audio.Media.IS_MUSIC,
            "\"" + MediaStore.Audio.Media.INTERNAL_CONTENT_URI + "\""
    };

    private static final String[] EXTERNAL_COLUMNS = new String[] {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.IS_RINGTONE,
            MediaStore.Audio.Media.IS_ALARM,
            MediaStore.Audio.Media.IS_NOTIFICATION,
            MediaStore.Audio.Media.IS_MUSIC,
            "\"" + MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "\""
    };

    public void onBackPressed()
    {
        finish();
//        Intent i=new Intent(getApplicationContext(),Mp3TagEditor.class);
//         startActivity(i);
    }
}
