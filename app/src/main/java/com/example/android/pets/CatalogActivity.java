/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final int PET_LOADER = 0;

    PetCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        //Find the ListView which will be populated with the pet data
        ListView petListView = (ListView) findViewById(R.id.list_view_pet);

        //Find and set empty view on the ListView so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        //Setup an Adapter to create a list item for each row of pet data in the Cursor
        //There is no pet data yet (until the loader finishes) so pass in null for the Cursor
        mCursorAdapter = new PetCursorAdapter(this, null);
        petListView.setAdapter(mCursorAdapter);

        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                //Append the id of the current pet to the content URI
                Uri currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);

                //Set the URI on the data field of the intent
                intent.setData(currentPetUri);

                startActivity(intent);
            }
        });

        //initialize Loader
        getLoaderManager().initLoader(PET_LOADER, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
/*    private void displayDatabaseInfo() {
        //include all columns in projection for the query
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT
        };

        //The cursor should not be interacting with the database directly
        *//**Cursor cursor = db.query(
         PetEntry.TABLE_NAME,
         projection,
         null,
         null,
         null,
         null,
         null);
         **//*

        //Get a Cursor that contains all rows from the pets table.
        Cursor cursor = getContentResolver().query(
                PetEntry.CONTENT_URI,   //The content URI of the pets table
                projection,             //The columns to return for each row
                null, null, null);

        //Find ListView to populate
        ListView petItems = (ListView) findViewById(R.id.list_view_pet);

        //Setup cursor adapter to create a list item for each orw of pet data in the Cursor
        mAdapter = new PetCursorAdapter(this, cursor);

        //Attach cursor adapter to the ListView
        petItems.setAdapter(mAdapter);


    }*/

    //Inserts hardcoded pet data into the database. For debugging only
    private void insertPet() {

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Inca");
        values.put(PetEntry.COLUMN_PET_BREED, "Greyhound");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 2);

        // Insert the new row using the ContentResolver
        //Receive the new content URI
        Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);

        if (newUri != null) {
            Toast.makeText(getApplicationContext(), getString(R.string.editor_insert_pet_successful), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.editor_insert_pet_failed),
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Delete all pets from the table
                deleteAllPets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Define a projection that specifies the columns from the table we care about
        String[] projection = new String[] {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED
        };

        //This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                PetEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mCursorAdapter.swapCursor(null);
    }

    private void deleteAllPets(){

            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(PetEntry.CONTENT_URI, null, null);

            if (rowsDeleted == 0) {
                // If no rows were affected, then there was an error with deleting the row
                Toast.makeText(this, "Error with deleting all pets",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, "All pets deleted",
                        Toast.LENGTH_SHORT).show();
            }


    }
}