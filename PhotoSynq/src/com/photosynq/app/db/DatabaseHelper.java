package com.photosynq.app.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.photosynq.app.model.Macro;
import com.photosynq.app.model.Option;
import com.photosynq.app.model.Protocol;
import com.photosynq.app.model.Question;
import com.photosynq.app.model.ResearchProject;

public class DatabaseHelper extends SQLiteOpenHelper {
	// Database Version
	private static final int DATABASE_VERSION = 1;
	// Database Name
	private static final String DATABASE_NAME = "PhotoSynqDB";
	// Table Names
	private static final String TABLE_RESEARCH_PROJECT = "research_project";
	private static final String TABLE_QUESTION = "question";
	private static final String TABLE_OPTION = "option";
	private static final String TABLE_PROTOCOL = "protocol";
	private static final String TABLE_MACRO = "macro";

	//column names
	public static final String C_RECORD_HASH = "record_hash";
	public static final String C_ID = "id";
	private static final String C_NAME = "name";
	private static final String C_DESCRIPTION = "description";
	private static final String C_SLUG =  "slug";
	
	// Research Project columns
	private static final String C_DIR_TO_COLLAB = "dir_to_collab";
	private static final String C_START_DATE = "start_date";
	private static final String C_END_DATE = "end_date";
	private static final String C_IMAGE_URL = "image_url";
	private static final String C_BETA = "beta";
	public static final String C_PROJECT_ID = "project_id";
	private static final String C_QUESTION_ID = "question_id";
	
	// Question and Option Table - column names
	private static final String C_QUESTION_TEXT = "question_text";//Question
	private static final String C_OPTION_TEXT = "option";//Option
	
	// Protocol column names

	private static final String C_QUICK_DESCRIPTION = "quick_description";
	private static final String C_PROTOCOL_NAME_IN_ARDUINO_CODE = "protocol_name_in_arduino_code";
	private static final String C_MACRO_ID = "macro_id";
	

	// Macro column names
	private static final String C_DEFAULT_X_AXIS = "default_x_axis";
	private static final String C_DEFAULT_Y_AXIS = "default_y_axis";
	private static final String C_JAVASCRIPT_CODE = "javascript_code";
	private static final String C_JSON_DATA = "json_data";
	
	
	
	// Reaserch Project table create statement
	//
	private static final String CREATE_TABLE_RESEARCH_PROJECT = "CREATE TABLE "
			+ TABLE_RESEARCH_PROJECT + "(" + C_RECORD_HASH
			+ " TEXT PRIMARY KEY," + C_ID + " TEXT," + C_NAME + " TEXT,"
			+ C_DESCRIPTION + " TEXT," + C_DIR_TO_COLLAB + " TEXT," + C_START_DATE
			+ " TEXT," + C_END_DATE + " TEXT," + C_BETA + " TEXT,"
			+ C_IMAGE_URL + " TEXT" + ")";

	// Question table create statement
	private static final String CREATE_TABLE_QUESTION = "CREATE TABLE "
			+ TABLE_QUESTION + "("+ C_RECORD_HASH + " TEXT ," + C_PROJECT_ID
			+ " TEXT,"+ C_QUESTION_ID + " TEXT," + C_QUESTION_TEXT + " TEXT )";
	
	// Answer table create statement
	private static final String CREATE_TABLE_OPTION = "CREATE TABLE "
			+ TABLE_OPTION + "(" + C_RECORD_HASH + " TEXT ," + C_OPTION_TEXT + " TEXT ,"+ C_PROJECT_ID
			+ " TEXT," + C_QUESTION_ID + " TEXT)" ;
	
	// Protocol table create statement
	private static final String CREATE_TABLE_PROTOCOL = "CREATE TABLE "
			+ TABLE_PROTOCOL + "(" + C_RECORD_HASH + " TEXT ,"+ C_ID + " TEXT ,"+ C_NAME + " TEXT ,"
			+ C_QUICK_DESCRIPTION + " TEXT ,"+ C_PROTOCOL_NAME_IN_ARDUINO_CODE + " TEXT ," 
			+ C_DESCRIPTION + " TEXT ,"+ C_MACRO_ID + " TEXT ," + C_SLUG + " TEXT)";

	// Macro table create statement
	private static final String CREATE_TABLE_MACRO = "CREATE TABLE "
			+ TABLE_MACRO + "(" + C_RECORD_HASH + " TEXT ,"+ C_ID + " TEXT ,"+ C_NAME + " TEXT ,"
			+ C_DESCRIPTION + " TEXT ,"+ C_DEFAULT_X_AXIS + " TEXT ,"+ C_DEFAULT_Y_AXIS + " TEXT ,"
			+ C_JAVASCRIPT_CODE + " TEXT ,"+ C_JSON_DATA + " TEXT ," + C_SLUG + " TEXT)";

	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// creating required tables
		db.execSQL(CREATE_TABLE_RESEARCH_PROJECT);
		db.execSQL(CREATE_TABLE_QUESTION);
		db.execSQL(CREATE_TABLE_OPTION);
		db.execSQL(CREATE_TABLE_PROTOCOL);
		db.execSQL(CREATE_TABLE_MACRO);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
		// on upgrade drop older tables
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESEARCH_PROJECT);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUESTION);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_OPTION);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROTOCOL);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MACRO);
		// create new tables;
		onCreate(db);

	}

	// Insert research project information in database
	public boolean createResearchProject(ResearchProject rp) {
		try
		{
			SQLiteDatabase db = this.getWritableDatabase();
											
			ContentValues values = new ContentValues();
			values.put(C_ID, null != rp.getId() ? rp.getId() : "");
			values.put(C_NAME, null != rp.getName() ? rp.getName() : "");
			values.put(C_DESCRIPTION, null != rp.getDescription() ? rp.getDescription() : "");
			values.put(C_DIR_TO_COLLAB,null != rp.getDirToCollab()? rp.getDirToCollab() : "");
			values.put(C_START_DATE,null != rp.getStartDate() ? rp.getStartDate() : "");
			values.put(C_END_DATE, null != rp.getEndDate()? rp.getEndDate(): "");
			values.put(C_BETA, null != rp.getBeta() ? rp.getBeta() : "");
			values.put(C_IMAGE_URL,null != rp.getImageUrl() ? rp.getImageUrl(): "");
			values.put(C_RECORD_HASH, rp.getRecordHash());
			// insert row
			long row_id = db.insert(TABLE_RESEARCH_PROJECT, null, values);
	
			if (row_id >= 0) {
				return true;
			} else {
				return false;
			}
		}catch (SQLiteConstraintException contraintException)
		{
			// If data already present then handle the case here.
			Log.d("DATABASE_HELPER_RESEARCH_PROJECTS", "Record already present in database for record hash ="+rp.getRecordHash());
			return false;
			
		}
		catch (SQLException sqliteException){
			return false;
		}

	}

	//Get research project information from database 
	public ResearchProject getResearchProject(String id ) {
	    SQLiteDatabase db = this.getReadableDatabase();
	 
	    String selectQuery = "SELECT  * FROM " + TABLE_RESEARCH_PROJECT + " WHERE "
	            + C_ID + " = '" + id + "'";
	 
	    Log.e("DATABASE_HELPER_getResearchProject", selectQuery);
	 
	    Cursor c = db.rawQuery(selectQuery, null);
	 
	    if (c != null)
	        c.moveToFirst();
	 
	    ResearchProject rp = new ResearchProject();
	    rp.setId(c.getString(c.getColumnIndex(C_ID)));
	    rp.setName(c.getString(c.getColumnIndex(C_NAME)));
	    rp.setDescription(c.getString(c.getColumnIndex(C_DESCRIPTION)));
	    rp.setDirToCollab(c.getString(c.getColumnIndex(C_DIR_TO_COLLAB)));
	    rp.setStartDate(c.getString(c.getColumnIndex(C_START_DATE)));
	    rp.setEndDate(c.getString(c.getColumnIndex(C_END_DATE)));
	    rp.setImageUrl(c.getString(c.getColumnIndex(C_IMAGE_URL)));
	    rp.setRecordHash(c.getString(c.getColumnIndex(C_RECORD_HASH)));
	    rp.setBeta(c.getString(c.getColumnIndex(C_BETA)));

	    c.close();
	    return rp;
	}
	
	//Get all research project information from database.
		public List<ResearchProject> getAllResearchProjects() {
		    SQLiteDatabase db = this.getReadableDatabase();
		    List<ResearchProject> researchProjects = new ArrayList<ResearchProject>();
		    String selectQuery = "SELECT  * FROM " + TABLE_RESEARCH_PROJECT ;
		 
		    Log.e("DATABASE_HELPER_getAllResearchProject", selectQuery);
		 
		    Cursor c = db.rawQuery(selectQuery, null);
		 
		    if (c.moveToFirst()) {
		        do {
		        	ResearchProject rp = new ResearchProject();
				    rp.setId(c.getString(c.getColumnIndex(C_ID)));
				    rp.setName(c.getString(c.getColumnIndex(C_NAME)));
				    rp.setDescription(c.getString(c.getColumnIndex(C_DESCRIPTION)));
				    rp.setDirToCollab(c.getString(c.getColumnIndex(C_DIR_TO_COLLAB)));
				    rp.setStartDate(c.getString(c.getColumnIndex(C_START_DATE)));
				    rp.setEndDate(c.getString(c.getColumnIndex(C_END_DATE)));
				    rp.setImageUrl(c.getString(c.getColumnIndex(C_IMAGE_URL)));
				    rp.setRecordHash(c.getString(c.getColumnIndex(C_RECORD_HASH)));
				    rp.setBeta(c.getString(c.getColumnIndex(C_BETA)));
		 
		            // adding to todo list
				    researchProjects.add(rp);
		        } while (c.moveToNext());
		    }

		    c.close();
		    return researchProjects;
		}
		
	/*
	 * Updating a Research Project
	 */
	public boolean updateResearchProject(ResearchProject rp) {
		

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(C_ID, null != rp.getId() ? rp.getId() : "");
		values.put(C_NAME, null != rp.getName() ? rp.getName() : "");
		values.put(C_DESCRIPTION, null != rp.getDescription()? rp.getDescription() : "");
		values.put(C_DIR_TO_COLLAB,null != rp.getDirToCollab()? rp.getDirToCollab() : "");
		values.put(C_START_DATE,null != rp.getStartDate() ? rp.getStartDate() : "");
		values.put(C_END_DATE, null != rp.getEndDate()? rp.getEndDate(): "");
		values.put(C_BETA, null != rp.getBeta() ? rp.getBeta() : "");
		values.put(C_IMAGE_URL,null != rp.getImageUrl() ? rp.getImageUrl(): "");
		values.put(C_RECORD_HASH, rp.getRecordHash());

		
		int rowsaffected = db.update(TABLE_RESEARCH_PROJECT, values, C_ID + " = ?",
							new String[] { String.valueOf(rp.getId()) });
		// if update fails that indicates there is no then create new row
		if(rowsaffected <= 0)
		{
			return createResearchProject(rp);
		}
		// updating row
		return false;
	}
	
	/*
	 * Deleting a research project
	 */
	public void deleteResearchProject(String id) {
	    SQLiteDatabase db = this.getWritableDatabase();
	    db.delete(TABLE_RESEARCH_PROJECT, C_ID + " = ?",
	            new String[] { String.valueOf(id) });
	}
	
	// Insert a question in database
	public boolean createQuestion(Question que) {
		try
		{
			SQLiteDatabase db = this.getWritableDatabase();
											
			ContentValues values = new ContentValues();
			values.put(C_RECORD_HASH, null != que.getRecordHash() ? que.getRecordHash() : "");
			values.put(C_QUESTION_TEXT, null != que.getQuestionText() ? que.getQuestionText() : "");
			values.put(C_QUESTION_ID, que.getQuestionId());
			values.put(C_PROJECT_ID, que.getProjectId());
			// insert row
			long row_id = db.insert(TABLE_QUESTION, null, values);
			System.out.println("This is row ID" + row_id);
			if (row_id >= 0) {
				return true;
			} else {
				return false;
			}
		}catch (SQLiteConstraintException contraintException)
		{
			// If data already present then handle the case here.
			return false;
		}
		catch (SQLException sqliteException){
			return false;
		}
	}
	
public boolean updateQuestion(Question question) {
		

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(C_RECORD_HASH, null != question.getRecordHash() ? question.getRecordHash() : "");
		values.put(C_QUESTION_ID, null != question.getQuestionId() ? question.getQuestionId() : "");
		values.put(C_QUESTION_TEXT, null != question.getQuestionText() ? question.getQuestionText() : "");
		values.put(C_PROJECT_ID, question.getProjectId());

		
		int rowsaffected = db.update(TABLE_QUESTION, values, C_QUESTION_ID + " = ? and "+C_PROJECT_ID + " =?",
							new String[] { String.valueOf(question.getQuestionId()),String.valueOf(question.getProjectId()) });
		// if update fails that indicates there is no then create new row
		if(rowsaffected <= 0)
		{
			return createQuestion(question);
		}
		// updating row
		return false;
	}


	//Insert Option in database 
	public boolean createOption(Option op) {
		try
		{
			SQLiteDatabase db = this.getWritableDatabase();
											
			ContentValues values = new ContentValues();
			values.put(C_RECORD_HASH,op.getRecordHash());
			values.put(C_OPTION_TEXT, null != op.getOptionText() ? op.getOptionText(): "");
			values.put(C_QUESTION_ID,null != op.getQuestionId() ? op.getQuestionId(): "");
			values.put(C_PROJECT_ID,null != op.getProjectId()? op.getProjectId(): "");
			// insert row
			long row_id = db.insert(TABLE_OPTION, null, values);
			if (row_id >= 0) {
				return true;
			} else {
				return false;
			}
		}catch (SQLiteConstraintException contraintException)
		{
			// If data already present then handle the case here.
			return false;
		}
		catch (SQLException sqliteException){
			return false;
		}
	}
	
	public boolean updateOption(Option option) {
		

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(C_RECORD_HASH,option.getRecordHash());
		values.put(C_OPTION_TEXT, null != option.getOptionText() ? option.getOptionText(): "");
		values.put(C_QUESTION_ID,null != option.getQuestionId() ? option.getQuestionId(): "");
		values.put(C_PROJECT_ID,null != option.getProjectId()? option.getProjectId(): "");

		
		int rowsaffected = db.update(TABLE_OPTION, values, C_RECORD_HASH + " = ?",
							new String[] { String.valueOf(option.getRecordHash()) });
		// if update fails that indicates there is no then create new row
		if(rowsaffected <= 0)
		{
			return createOption(option);
		}
		// updating row
		return false;
	}

	// Get list of all questions for given project.
	public List<Question> getAllQuestionForProject(String project_id) {
	    SQLiteDatabase db = this.getReadableDatabase();
	    List<Question> questions = new ArrayList<Question>();
	    String selectQuery = "SELECT  * FROM " + TABLE_QUESTION + " WHERE " + C_PROJECT_ID+ " = " + project_id;
	    
	    
	    
	    System.out.println(selectQuery);
	    Log.e("DATABASE_HELPER_getAllQuestion", selectQuery);
	 
	    Cursor c = db.rawQuery(selectQuery, null);
	 
	    if (c.moveToFirst()) {
	        do {
	        	Question que = new Question();
	        	String questionId = c.getString(c.getColumnIndex(C_QUESTION_ID));
			    que.setQuestionText(c.getString(c.getColumnIndex(C_QUESTION_TEXT)));
			    que.setProjectId(c.getString(c.getColumnIndex(C_PROJECT_ID)));
			    que.setQuestionId(questionId);
			    que.setRecordHash(c.getString(c.getColumnIndex(C_RECORD_HASH)));
			    
			    
			   
			    // adding to todo list
			    if(!que.getQuestionText().equals(""))
			    {
				    String selectOptionsQuery = "SELECT  * FROM " + TABLE_OPTION + " WHERE " + C_QUESTION_ID + " = " + questionId +" and "+C_PROJECT_ID +" = "+project_id;
				    System.out.println("############ selectOptionsQuery : "+selectOptionsQuery);
				    
				    Cursor optionCursor = db.rawQuery(selectOptionsQuery, null);
				    if(optionCursor.moveToFirst())
				    {
				    	List<String> optionlist = new ArrayList<String>();
				    	do
				    	{
				    		String option = optionCursor.getString(optionCursor.getColumnIndex(C_OPTION_TEXT));
				    		System.out.println("######### $$$ option "+option);
				    		optionlist.add(option);
				    		
				    	}while(optionCursor.moveToNext());
				    	
				    	que.setOptions(optionlist);
				    	optionCursor.close();
				    	
				    }
			    	questions.add(que);
			    }
	        } while (c.moveToNext());
	        
	    }
	    c.close();

	    return questions;
	}
		
	//Insert Option in database 
	public boolean createProtocol(Protocol protocol) {
		try
		{
			SQLiteDatabase db = this.getWritableDatabase();
										
			ContentValues values = new ContentValues();
			values.put(C_RECORD_HASH,protocol.getRecordHash());
			values.put(C_ID, null != protocol.getId()? protocol.getId(): "");
			values.put(C_NAME, null != protocol.getName()? protocol.getName(): "");
			values.put(C_QUICK_DESCRIPTION, null != protocol.getQuickDescription()? protocol.getQuickDescription(): "");
			values.put(C_PROTOCOL_NAME_IN_ARDUINO_CODE, null != protocol.getProtocolNameInArduino_code()? protocol.getProtocolNameInArduino_code(): "");
			values.put(C_DESCRIPTION, null != protocol.getDescription()? protocol.getDescription(): "");
			values.put(C_MACRO_ID, null != protocol.getMacroId()? protocol.getMacroId(): "");
			values.put(C_SLUG,null != protocol.getSlug() ? protocol.getSlug(): "");
			// insert row
			long row_id = db.insert(TABLE_PROTOCOL, null, values);
			if (row_id >= 0) {
				return true;
			} else {
				return false;
			}
		}catch (SQLiteConstraintException contraintException)
		{
			// If data already present then handle the case here.
			return false;
		}
		catch (SQLException sqliteException){
			return false;
		}
	}

	//Get all protocols 
	public List<Protocol> getAllProtocolsList() {
	    SQLiteDatabase db = this.getReadableDatabase();
	    List<Protocol> protocols = new ArrayList<Protocol>();
	    String selectQuery = "SELECT  * FROM " + TABLE_PROTOCOL ;
	    System.out.println(selectQuery);
	    Log.e("DATABASE_HELPER_getAllProtocol", selectQuery);
	 
	    Cursor c = db.rawQuery(selectQuery, null);
	 
	    if (c.moveToFirst()) {
	        do {
	        	Protocol protocol = new Protocol();
			    protocol.setRecordHash(c.getString(c.getColumnIndex(C_RECORD_HASH)));
			    protocol.setId(c.getString(c.getColumnIndex(C_ID)));
			    protocol.setDescription(c.getString(c.getColumnIndex(C_DESCRIPTION)));
			    protocol.setName(c.getString(c.getColumnIndex(C_NAME)));
			    protocol.setQuickDescription(c.getString(c.getColumnIndex(C_QUICK_DESCRIPTION)));
			    protocol.setProtocolNameInArduino_code(c.getString(c.getColumnIndex(C_PROTOCOL_NAME_IN_ARDUINO_CODE)));
			    protocol.setSlug(c.getString(c.getColumnIndex(C_SLUG)));
			    protocol.setMacroId(c.getString(c.getColumnIndex(C_MACRO_ID)));
			    
			    // adding to todo list
			    protocols.add(protocol);
	        } while (c.moveToNext());
	    }
	    c.close();
	    return protocols;

	}
	
	
	//Insert Macro in database 
	public boolean createMacro(Macro macro) {
		try
		{
			SQLiteDatabase db = this.getWritableDatabase();
										
			ContentValues values = new ContentValues();
			values.put(C_RECORD_HASH,macro.getRecordHash());
			values.put(C_ID, null != macro.getId()? macro.getId(): "");
			values.put(C_NAME, null != macro.getName()? macro.getName(): "");
			values.put(C_DESCRIPTION, null != macro.getDescription()? macro.getDescription(): "");
			values.put(C_SLUG,null != macro.getSlug() ? macro.getSlug(): "");
			values.put(C_DEFAULT_X_AXIS,null != macro.getDefaultXAxis() ? macro.getDefaultXAxis(): "");
			values.put(C_DEFAULT_Y_AXIS,null != macro.getDefaultYAxis() ? macro.getDefaultYAxis(): "");
			values.put(C_JAVASCRIPT_CODE,null != macro.getJavascriptCode()? macro.getJavascriptCode(): "");
			values.put(C_JSON_DATA,null != macro.getJsonData() ? macro.getJsonData(): "");
			// insert row
			long row_id = db.insert(TABLE_MACRO, null, values);
			if (row_id >= 0) {
				return true;
			} else {
				return false;
			}
		}catch (SQLiteConstraintException contraintException)
		{
			// If data already present then handle the case here.
			return false;
		}
		catch (SQLException sqliteException){
			return false;
		}
	}
    //get macro from database
	public Macro getMacro(String id ) {
	    SQLiteDatabase db = this.getReadableDatabase();
	 
	    String selectQuery = "SELECT  * FROM " + TABLE_MACRO + " WHERE "
	            + C_ID + " = '" + id + "'";
	 
	    Log.e("DATABASE_HELPER_getMacro", selectQuery);
	 
	    Cursor c = db.rawQuery(selectQuery, null);
	 
	    if (c != null)
	        c.moveToFirst();
	 
	    Macro macro = new Macro();
	    macro.setId(c.getString(c.getColumnIndex(C_ID)));
	    macro.setName(c.getString(c.getColumnIndex(C_NAME)));
	    macro.setDescription(c.getString(c.getColumnIndex(C_DESCRIPTION)));
	    macro.setRecordHash(c.getString(c.getColumnIndex(C_RECORD_HASH)));
	    macro.setSlug(c.getString(c.getColumnIndex(C_SLUG)));
	    macro.setDefaultXAxis(c.getString(c.getColumnIndex(C_DEFAULT_X_AXIS)));
	    macro.setDefaultYAxis(c.getString(c.getColumnIndex(C_DEFAULT_Y_AXIS)));
	    macro.setJavascriptCode(c.getString(c.getColumnIndex(C_JAVASCRIPT_CODE)));
	    macro.setJsonData(c.getString(c.getColumnIndex(C_JSON_DATA)));
	    return macro;
	}
	
	// closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }
    
	
}
