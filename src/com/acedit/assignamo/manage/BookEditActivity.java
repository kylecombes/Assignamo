package com.acedit.assignamo.manage;

import com.acedit.assignamo.R;
import com.acedit.assignamo.database.DbAdapter;
import com.acedit.assignamo.database.Values;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BookEditActivity extends Activity {
	
	private Long rowId;
	private DbAdapter bookDbAdapter;
	static final String DATE_FORMAT_DISPLAY = "yyyy-MM-dd";
	static final String DATE_FORMAT_SAVE = "MM/dd/yyyy";
	static final String TIME_FORMAT_DISPLAY = "kk:mm";
	static final String TIME_FORMAT_SAVE = "dd:mm a";
	static final String DATE_TIME_FORMAT = "yyyy-MM-dd kk:mm:ss";
	
	protected Object selectedCourse;
	private TextView titleField;
	private TextView authorField;
	private TextView descriptionField;
	private Spinner typeSpinner;
	private TextView chaptersField;
	private TextView pagesField;
	private TextView ISBNField;
	private Button saveButton;
	private Button cancelButton;
		
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.book_edit);
		
		setRowIdFromIntent();
		initializeFields();
		populateFields();
		initializeButtons();
		
		titleField.requestFocus();
	}
	
	public void onResume() {
		super.onResume();
		bookDbAdapter = new DbAdapter(this,
				Values.DATABASE_NAME,
				Values.DATABASE_VERSION,
				Values.BOOK_TABLE,
				Values.DATABASE_CREATE,
				Values.KEY_ROWID);
		bookDbAdapter.open();
	}
	
	public void onPause() {
		super.onPause();
		bookDbAdapter.close();
	}
	
	private void initializeFields() {
		titleField = (TextView)findViewById(R.id.add_book_title_field);
		authorField = (TextView)findViewById(R.id.add_book_author_field);
		descriptionField = (TextView)findViewById(R.id.add_book_description_field);
		pagesField = (TextView)findViewById(R.id.add_book_pages_field);
		chaptersField = (TextView)findViewById(R.id.add_book_chapters_field);
		ISBNField = (TextView)findViewById(R.id.add_book_ISBN_field);
		TableLayout chaptersPointsTable = (TableLayout)findViewById(R.id.add_book_pages_chapters_table);
		chaptersPointsTable.setColumnStretchable(0, true);
		chaptersPointsTable.setColumnStretchable(1, true);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				  this, R.array.book_types, android.R.layout.simple_spinner_item );
				adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
		typeSpinner = (Spinner)findViewById(R.id.add_book_type_spinner);
		typeSpinner.setAdapter(adapter);
		
	    typeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	    	public void onItemSelected(AdapterView<?> parent,
	    	        View view, int pos, long id) {
	    		selectedCourse = parent.getItemAtPosition(pos);
	    	}
	    	
	    	public void onNothingSelected(AdapterView<?> parent) {
	    		
	    	}
	    });
	}
	
	private void initializeButtons() {
		
		saveButton = (Button)findViewById(R.id.book_edit_save_button);
	    saveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if ( saveData() )
					finish();
			}
		});
	    
		cancelButton = (Button)findViewById(R.id.book_edit_cancel_button);
	    cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private void setRowIdFromIntent() {
		if (rowId == null) {
			Bundle extras = getIntent().getExtras();
			rowId = extras != null
					? extras.getLong(Values.KEY_ROWID)
					: null;
		}
	}
	
	private void populateFields() {
		if (rowId != null) {
			DbAdapter db = new DbAdapter(this,
					Values.DATABASE_NAME,
					Values.DATABASE_VERSION,
					Values.BOOK_TABLE,
					Values.DATABASE_CREATE,
					Values.KEY_ROWID);
			db.open();
			Cursor cursor = db.fetch(rowId, Values.BOOK_FETCH);
			titleField.setText(cursor.getString(cursor.getColumnIndexOrThrow(Values.KEY_TITLE)));
			authorField.setText(cursor.getString(cursor.getColumnIndexOrThrow(Values.BOOK_KEY_AUTHOR)));
			descriptionField.setText(cursor.getString(cursor.getColumnIndexOrThrow(Values.KEY_DESCRIPTION)));
			long pages = cursor.getLong(cursor.getColumnIndexOrThrow(Values.BOOK_KEY_PAGES));
			if (pages > 0)
				pagesField.setText("" + pages);
			long chapters = cursor.getLong(cursor.getColumnIndexOrThrow(Values.BOOK_KEY_CHAPTERS));
			if (chapters > 0)
				chaptersField.setText("" + chapters);
			typeSpinner.setSelection(cursor.getShort(cursor.getColumnIndexOrThrow(Values.BOOK_KEY_TYPE)));
			long ISBN = cursor.getLong(cursor.getColumnIndexOrThrow(Values.BOOK_KEY_ISBN));
			if (ISBN > 0)
				ISBNField.setText("" + ISBN);
			db.close();
		}
	}
	
	private boolean saveData() {
		
		// Parse ISBN
		if (ISBNField.length() != 0 && ISBNField.length() != 10 && ISBNField.length() != 13) {
			Toast.makeText(this, R.string.add_book_invalid_ISBN, Toast.LENGTH_SHORT).show();
			return false;
		}
		
		String pgStr = pagesField.getText().toString();
		long pages = pgStr.length() > 0 ? Long.parseLong(pgStr) : 0;
		String chapStr = chaptersField.getText().toString();
		long chapters = chapStr.length() > 0 ? Long.parseLong(chapStr) : 0;
		String isbnStr = ISBNField.getText().toString();
		long ISBN = isbnStr.length() > 0 ? Long.parseLong(isbnStr) : 0;
		
		if (rowId == null)
			addBook(titleField.getText().toString(),
					authorField.getText().toString(),
					descriptionField.getText().toString(),
					(short)typeSpinner.getSelectedItemPosition(),
					pages, chapters, ISBN);
		else
			updateBook(titleField.getText().toString(),
					authorField.getText().toString(),
					descriptionField.getText().toString(),
					(short)typeSpinner.getSelectedItemPosition(),
					pages, chapters, ISBN, rowId);
		return true;
		
	}
	
    private long addBook(String title, String author, String description, short type, long pages, long chapters, long ISBN) {
    	ContentValues values = new ContentValues();
    	values.put(Values.KEY_TITLE, title);
    	values.put(Values.BOOK_KEY_AUTHOR, author);
    	values.put(Values.KEY_DESCRIPTION, description);
    	values.put(Values.BOOK_KEY_TYPE, type);
    	values.put(Values.BOOK_KEY_PAGES, pages);
    	values.put(Values.BOOK_KEY_CHAPTERS, chapters);
    	values.put(Values.BOOK_KEY_ISBN, ISBN);
    	return bookDbAdapter.add(values);
    }
    
    private boolean updateBook(String title, String author, String description, short type, long pages, long chapters, long ISBN, long rowId) {
    	ContentValues values = new ContentValues();
    	values.put(Values.KEY_TITLE, title);
    	values.put(Values.BOOK_KEY_AUTHOR, author);
    	values.put(Values.KEY_DESCRIPTION, description);
    	values.put(Values.BOOK_KEY_TYPE, type);
    	values.put(Values.BOOK_KEY_PAGES, pages);
    	values.put(Values.BOOK_KEY_CHAPTERS, chapters);
    	values.put(Values.BOOK_KEY_ISBN, ISBN);
    	return bookDbAdapter.update(rowId, values);
    }
}
