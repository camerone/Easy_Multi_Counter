package com.hunterdavis.easymulticounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class EasyMultiCounter extends Activity {

	InventorySQLHelper counterData = new InventorySQLHelper(this);
	String currentCountName = "";
	int currentIdCounter = 100;
	int currentId = 0;
	String textChangedListenerText = "";
	int mutex = 33;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Create an anonymous implementation of OnClickListener
		OnClickListener newButtonListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked

				// in onCreate or any event where your want the user to

				AlertDialog.Builder alert = new AlertDialog.Builder(
						v.getContext());

				alert.setTitle("Counter Name");
				alert.setMessage("Please Enter A Name For The New Counter");

				// Set an EditText view to get user input
				final EditText input = new EditText(v.getContext());
				alert.setView(input);

				alert.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String tempName = input.getText().toString();

								Cursor tempCursor = getCountCursorByName(tempName);
								if (tempCursor.getCount() > 0) {
									Toast.makeText(getBaseContext(),
											"That Name is Already In Use!",
											Toast.LENGTH_LONG).show();
								} else {
									// Do something with value!
									if (tempName.length() > 1) {
										// select a file
										CreateNewRow(tempName, 0);
									} else {
										Toast.makeText(getBaseContext(),
												"Invalid Name!",
												Toast.LENGTH_LONG).show();
									}
								}
							}

						});

				alert.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// Canceled.
							}
						});

				alert.show();

			}
		};

		Button newButton = (Button) findViewById(R.id.addbutton);
		newButton.setOnClickListener(newButtonListner);

		// fill up our view
		Cursor cursor = getCountCursor();
		if (cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				CreatePhysicalRow(cursor.getString(1), cursor.getFloat(2));
			}
		}

		// Look up the AdView as a resource and load a request.
		AdView adView = (AdView) this.findViewById(R.id.adView);
		adView.loadAd(new AdRequest());
	} // end of oncreate

	public void CreateNewRow(String name, float val) {
		// now that we have a picture uri, create a new table entry for
		// this inventory item
		SQLiteDatabase db = counterData.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(InventorySQLHelper.NAME, name);
		values.put(InventorySQLHelper.COUNT, val);
		long latestRowId = db.insert(InventorySQLHelper.TABLE, null, values);
		db.close();

		// now that we have a new row in the database, add the physical row
		CreatePhysicalRow(name, val);
	}

	public void CreatePhysicalRow(String name, float val) {
		currentIdCounter++;

		int delbuttonId = currentIdCounter;
		int tablerowid = delbuttonId + 6000;
		int minusbuttonid = delbuttonId + 1000;
		int plusbuttotid = minusbuttonid + 1000;
		int edittextid = delbuttonId + 3000;
		int nameid = delbuttonId + 4000;

		// Get the TableLayout
		TableLayout tl = (TableLayout) findViewById(R.id.maintable);

		// Create a TableRow and give it an ID
		TableRow tr = new TableRow(this);
		tr.setId(tablerowid);
		tr.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));

		// create our delete button
		Button delButton = new Button(this);
		delButton.setId(delbuttonId);
		delButton.setText("X");
		delButton.setTextColor(Color.BLACK);
		delButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		tr.addView(delButton);

		OnClickListener DeleteButtonListner = new OnClickListener() {
			public void onClick(View v) {
				String name = "put name here";
				yesnoDeleteHandler("Are you sure?",
						"Are you sure you want to delete?", v.getId() + 4000);
			}
		};

		delButton.setOnClickListener(DeleteButtonListner);

		// create our minus button
		Button minusButton = new Button(this);
		minusButton.setId(minusbuttonid);
		minusButton.setText("-");
		minusButton.setTextColor(Color.BLACK);
		minusButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		tr.addView(minusButton);

		OnClickListener minusButtonListner = new OnClickListener() {
			public void onClick(View v) {
				EditText numtxt = (EditText) findViewById(v.getId() + 2000);
				float newval = Float.valueOf(numtxt.getText().toString()) - 1;
				mutex = 0;
				numtxt.setText(String.valueOf(newval));

				EditText nametxt = (EditText) findViewById(v.getId() + 3000);

				updateDatabaseValue(nametxt.getText().toString().trim(), newval);
			}
		};

		minusButton.setOnClickListener(minusButtonListner);

		// create our editview numerical
		//
		EditText numericalEdit = new EditText(this);
		numericalEdit.setId(edittextid);
		numericalEdit.setText(String.valueOf(val));
		numericalEdit.setTextColor(Color.BLACK);
		numericalEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
		numericalEdit.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		tr.addView(numericalEdit);

		// numerical edit listener
		// name listener
		numericalEdit.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if(mutex == 0)
				{
					mutex = 33;
					return;
				}
				// here we find the old name
				EditText myNumEdit = (EditText) getCurrentFocus();
				int myNumEditId = myNumEdit.getId();
				EditText myNameEdit = (EditText) findViewById(myNumEditId + 1000);
				String name = myNameEdit.getText().toString().trim();
				// here we call the text changed update sql function
				String floatString = s.toString();
				float value;
				try {
					value = Float.valueOf(textChangedListenerText);
				} catch (Exception e) {
				}

				try {
					value = Float.valueOf(floatString); 
				} catch (Exception e) {
					myNumEdit.setText(textChangedListenerText);
					return;
				}
				updateDatabaseValue(name, value);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				textChangedListenerText = String.valueOf(s);

			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		// create our plus button
		Button plusButton = new Button(this);
		plusButton.setId(plusbuttotid);
		plusButton.setText("+");
		plusButton.setTextColor(Color.BLACK);
		plusButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		tr.addView(plusButton);

		OnClickListener plusButtonListner = new OnClickListener() {
			public void onClick(View v) {
				EditText numtxt = (EditText) findViewById(v.getId() + 1000);
				mutex = 0;
				float newval = Float.valueOf(numtxt.getText().toString()) + 1;
				numtxt.setText(String.valueOf(newval));

				EditText nametxt = (EditText) findViewById(v.getId() + 2000);

				updateDatabaseValue(nametxt.getText().toString().trim(), newval);
			}
		};

		plusButton.setOnClickListener(plusButtonListner);

		// create our editview name
		//
		EditText nameEdit = new EditText(this);
		nameEdit.setId(nameid);
		nameEdit.setText(name);
		nameEdit.setTextColor(Color.BLACK);
		nameEdit.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		tr.addView(nameEdit);

		// name listener
		nameEdit.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				// here we call the text changed update sql function
				updateDatabaseName(textChangedListenerText, s.toString().trim());
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				textChangedListenerText = String.valueOf(s).trim();
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		// Add the TableRow to the TableLayout
		tl.addView(tr, new TableLayout.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));

	}

	public void DeleteCountByName(String countId) {

		SQLiteDatabase db = counterData.getWritableDatabase();
		db.delete(InventorySQLHelper.TABLE, "name = '" + countId + "'", null);
		db.close();

		// here we need to delete the physical elements from the screen
		TableLayout t = (TableLayout) findViewById(R.id.maintable);
		TableRow tr = (TableRow) findViewById(currentId + 2000);
		t.removeView(tr);
	}

	private Cursor getCountCursor() {
		SQLiteDatabase db = counterData.getReadableDatabase();
		Cursor cursor = db.query(InventorySQLHelper.TABLE, null, null, null,
				null, null, null);
		startManagingCursor(cursor);
		return cursor;
	}

	private Cursor getCountCursorByName(String rowId) {
		SQLiteDatabase db = counterData.getReadableDatabase();
		Cursor cursor = db.query(InventorySQLHelper.TABLE, null, "name = '"
				+ rowId + "'", null, null, null, null);
		startManagingCursor(cursor);
		return cursor;
	}

	private void updateDatabaseName(String oldname, String newname) {
		SQLiteDatabase db = counterData.getWritableDatabase();
		ContentValues args = new ContentValues();
		args.put(InventorySQLHelper.NAME, newname);
		String strFilter = " name='" + oldname + "'";
		db.update(InventorySQLHelper.TABLE, args, strFilter, null);
	}

	private void updateDatabaseValue(String name, float value) {
		SQLiteDatabase db = counterData.getWritableDatabase();
		ContentValues args = new ContentValues();
		args.put(InventorySQLHelper.COUNT, String.valueOf(value));
		String strFilter = " name='" + name + "'";
		db.update(InventorySQLHelper.TABLE, args, strFilter, null);
	}

	protected void yesnoDeleteHandler(String title, String mymessage, int ida) {
		EditText myEdit = (EditText) findViewById(ida);

		currentCountName = myEdit.getText().toString().trim();
		currentId = ida;

		new AlertDialog.Builder(this)
				.setMessage(mymessage)
				.setTitle(title)
				.setCancelable(true)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								DeleteCountByName(currentCountName);

							}
						})
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

} // end of file