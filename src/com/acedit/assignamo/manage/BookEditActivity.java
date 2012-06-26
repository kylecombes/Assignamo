package com.acedit.assignamo.manage;

import com.acedit.assignamo.database.DbAdapter;
import com.acedit.assignamo.database.Values;
import com.awesomeapplets.assignamo.R;

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
	    	@Override
	    	public void onItemSelected(AdapterView<?> parent,
	    	        View view, int pos, long id) {
	    		selectedCourse = parent.getItemAtPosition(pos);
	    	}
	    	
	    	@Override
	    	public void onNothingSelected(AdapterView<?> parent) {
	    		
	    	}
	    });
	}
	
	private void initializeButtons() {
		
		saveButton = (Button)findViewById(R.id.book_add_save_button);
	    saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( saveData() )
					finish();
			}
		});
	    
		cancelButton = (Button)findViewById(R.id.book_add_cancel_button);
	    cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
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
			Cursor bookData = db.fetch(rowId, Values.BOOK_FETCH);
			titleField.setText(bookData.getString(bookData.getColumnIndexOrThrow(Values.KEY_TITLE)));
			authorField.setText(bookData.getString(bookData.getColumnIndexOrThrow(Values.BOOK_KEY_AUTHOR)));
			descriptionField.setText(bookData.getString(bookData.getColumnIndexOrThrow(Values.KEY_DESCRIPTION)));
			pagesField.setText("" + bookData.getLong(bookData.getColumnIndexOrThrow(Values.BOOK_KEY_PAGES)));
			chaptersField.setText("" + bookData.getString(bookData.getColumnIndexOrThrow(Values.BOOK_KEY_CHAPTERS)));
			typeSpinner.setSelection(bookData.getShort(bookData.getColumnIndexOrThrow(Values.BOOK_KEY_TYPE)));
			ISBNField.setText("" + bookData.getString(bookData.getColumnIndexOrThrow(Values.BOOK_KEY_ISBN)));
		}
	}
	
	private boolean saveData() {
		
		// Parse ISBN
		if (ISBNField.length() != 10 && ISBNField.length() != 13) {
			Toast.makeText(this, R.string.add_book_invalid_ISBN, Toast.LENGTH_SHORT).show();
			return false;
		}
		if (rowId == null)
			addBook(titleField.getText().toString(),
					authorField.getText().toString(),
					descriptionField.getText().toString(),
					(short)typeSpinner.getSelectedItemPosition(),
					Long.parseLong(pagesField.getText().toString()),
					Long.parseLong(chaptersField.getText().toString()),
					Long.parseLong(ISBNField.getText().toString()));
		else
			updateBook(titleField.getText().toString(),
					authorField.getText().toString(),
					descriptionField.getText().toString(),
					(short)typeSpinner.getSelectedItemPosition(),
					Long.parseLong(pagesField.getText().toString()),
					Long.parseLong(chaptersField.getText().toString()),
					Long.parseLong(ISBNField.getText().toString()),
					rowId);
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
