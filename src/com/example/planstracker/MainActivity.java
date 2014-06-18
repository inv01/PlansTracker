package com.example.planstracker;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.example.planstracker.PlansDbHelper.DbEn;

import jxl.CellView;
import jxl.Workbook;
import jxl.write.DateFormat;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.widget.Toast;
import android.widget.CalendarView.OnDateChangeListener;

public class MainActivity extends FragmentActivity 
                          implements DetailsFragment.OnTaskChangedListener{
  private TaskListFragment taskList;
  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        if (savedInstanceState == null) {
            taskList = new TaskListFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, taskList).commit();
        } else {
            taskList = (TaskListFragment) getSupportFragmentManager().
                    findFragmentById(R.id.container);
        }
        CalendarView cal = (CalendarView) findViewById(R.id.calendar);
        if (cal != null){
            cal.setOnDateChangeListener(new OnDateChangeListener() {
            @Override
              public void onSelectedDayChange(CalendarView view, int year, int month,
              int dayOfMonth) {
              onDateSet(view, year, month, dayOfMonth);
              }
            });
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(cal.getDate()));
            setDateToPrefs(new Date(cal.getDate()));
        }
    }
    
    public void onDateSet(CalendarView view, int year, int month,
            int dayOfMonth){
        GregorianCalendar gc = new GregorianCalendar(year, month, dayOfMonth);
        setDateToPrefs(gc.getTime());
        onTaskChanged();
    }
    
    public void setDateToPrefs(Date d){
        SharedPreferences app_preferences = 
                PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = app_preferences.edit();
        editor.putLong("date", d.getTime());
        editor.commit();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_export) {
            String e_state = Environment.getExternalStorageState();
            if (!Environment.MEDIA_MOUNTED.equals(e_state)) {
                Toast.makeText(this, "SD card not available.", 
                        Toast.LENGTH_SHORT).show();
                return true;
            }
            new ListXLSExport(this).execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public boolean getIsDual() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
           // Landscape
            return true;
        }
        else {
           // Portrait
            return false;
        }
        //return getArguments().getBoolean("isdual", false);
    }
    
    @Override
    public void onTaskChanged() {
        taskList.setAdapter();
    }

    private class ListXLSExport extends AsyncTask<Void, Integer, String> {

        private WritableWorkbook workbook;
        
        private void createExcel() throws IOException, WriteException, RowsExceededException{
            File file_printable = new File(
                    Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            + "/" + reportXLSname);
            workbook = Workbook.createWorkbook(file_printable);
        }
       
       private WritableSheet getInitedSheet() throws RowsExceededException, WriteException{
           WritableSheet sheet = workbook.createSheet("Work list", 0);
           //"Date;Person;Phone;Email;Hours;Price;Note;Location;loc latitude;loc longitude;Picture
           sheet.addCell(new Label(0, 0, "Date"));
           sheet.addCell(new Label(1, 0, "Person"));
           sheet.addCell(new Label(2, 0, "Phone"));
           sheet.addCell(new Label(3, 0, "Email"));
           sheet.addCell(new Label(4, 0, "Hours"));
           sheet.addCell(new Label(5, 0, "Price"));
           sheet.addCell(new Label(6, 0, "Note"));
           sheet.addCell(new Label(7, 0, "Location"));
           sheet.addCell(new Label(8, 0, "loc.latitude"));
           sheet.addCell(new Label(9, 0, "loc.longitude"));
           sheet.addCell(new Label(10, 0, "Picture"));
           for(int i=0; i < 9; i++) {
               CellView cell = sheet.getColumnView(i);
               cell.setAutosize(true);
               sheet.setColumnView(i, cell);
           }
           return sheet;
       }
      @Override
      protected String doInBackground(Void... params) {
          String result = "";
          File download = Environment
                  .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
          PlansDbHelper mDbHelper = new PlansDbHelper(mContext);
          SQLiteDatabase db = mDbHelper.getReadableDatabase();
          //"Date;Person;Phone;Email;Hours;Price;Note;Location;loc latitude;loc longitude;Picture
          String sql = "Select " + DbEn.CN_DATE + 
                  ", ifnull(" + DbEn.CN_PERSON + ",'') " + DbEn.CN_PERSON + 
                  ", ifnull(" + DbEn.CN_PHONE + ",'') " + DbEn.CN_PHONE + 
                  ", ifnull(" + DbEn.CN_EMAIL + ",'') " + DbEn.CN_EMAIL + 
                  ", " + DbEn.CN_HOURS + 
                  ", " + DbEn.CN_MONEY + 
                  ", ifnull(" + DbEn.CN_NOTE + ",'') " + DbEn.CN_NOTE + 
                  ", ifnull(" + DbEn.CN_LOC_NAME + ",'') " + DbEn.CN_LOC_NAME + 
                  ", " + DbEn.CN_LOC_LAT + ", "
                  + DbEn.CN_LOC_LNG + ", "
                  + DbEn.CN_PICTURE + 
                  " from " + DbEn.TABLE_TPLAN + 
                  " order by " + DbEn.CN_DATE;
          
          Cursor c = db.rawQuery(sql, null);
          if(c.getCount() > 0)
          try{
              p.setMax(c.getCount());
              createExcel();
              int r = 0;
              WritableSheet sheet = getInitedSheet();
              while (c.moveToNext()) {
                  p.setProgress(r++);
                  //"Date;Person;Phone;Email;Hours;Price;Note;Location;loc latitude;loc longitude;Picture
                  Date now = new Date(c.getLong(c.getColumnIndex(DbEn.CN_DATE))); 
                  DateFormat customDateFormat = new DateFormat ("dd MMM yyyy hh:mm"); 
                  WritableCellFormat dateFormat = new WritableCellFormat (customDateFormat); 
                  DateTime dateCell = new DateTime(0, r, now, dateFormat); 
                  sheet.addCell(dateCell);
                  sheet.addCell(new Label(1, r, c.getString(c.getColumnIndex(DbEn.CN_PERSON))));
                  sheet.addCell(new Label( 2, r, c.getString(c.getColumnIndex(DbEn.CN_PHONE))));
                  sheet.addCell(new Label(3, r, c.getString(c.getColumnIndex(DbEn.CN_EMAIL))));
                  sheet.addCell(new Number(4, r, c.getDouble(c.getColumnIndex(DbEn.CN_HOURS))));
                  sheet.addCell(new Number(5, r, c.getDouble(c.getColumnIndex(DbEn.CN_MONEY))));
                  sheet.addCell(new Label(6, r, c.getString(c.getColumnIndex(DbEn.CN_NOTE))));
                  sheet.addCell(new Label(7, r, c.getString(c.getColumnIndex(DbEn.CN_LOC_NAME))));
                  sheet.addCell(new Number(8, r, c.getDouble(c.getColumnIndex(DbEn.CN_LOC_LAT))));
                  sheet.addCell(new Number(9, r, c.getDouble(c.getColumnIndex(DbEn.CN_LOC_LNG))));
                  byte[] bmp = c.getBlob(c.getColumnIndex(DbEn.CN_PICTURE));
                  if (bmp != null){
                      WritableImage imgobj = new WritableImage(10, r,
                              3, 8, bmp);
                      r+=7;
                      sheet.addImage(imgobj);
                  }
              }
              c.close();
              
              db.close();
              workbook.write(); 
              workbook.close();
              File fileExcelInDownloads = new File(download + "/" + reportXLSname);
              try {
                  download.mkdirs();
                  Intent intent = new Intent(
                          Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                  intent.setData(Uri.fromFile(fileExcelInDownloads));
                  sendBroadcast(intent);
              } catch (Exception ex) {
                  Log.e(ex.getLocalizedMessage(), 
                          getResources().getString(R.string.print_not_exp));
                  ex.printStackTrace();
              }
              result = "xls";
          } catch (Exception e) {
              Log.e(e.getLocalizedMessage(),
                      getResources().getString(R.string.print_not_exp));
              e.printStackTrace();
          }
          return result;
      }

      private Context mContext;
      private String reportXLSname = "PlansList.xls";
      private ProgressDialog p;
      
      public ListXLSExport(Context context) {
          mContext = context;
          this.p = new ProgressDialog(context);
      }

      @Override
      protected void onPreExecute() {
          Log.i("ListXLSExport", "onPreExecute()");
          super.onPreExecute();
          p.setMessage(getResources().getString(R.string.exporting));
          p.setIndeterminate(false);
          p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
          p.setCancelable(false);
          p.show();
      }

      @Override
      protected void onPostExecute(String result) {
          p.dismiss();
          if (result.equals("xls"))
              Toast.makeText(mContext, 
                  reportXLSname + " " + 
                  getResources().getString(R.string.saved_in_downloads), 
                  Toast.LENGTH_SHORT).show();
      }

      @Override
      protected void onProgressUpdate(Integer... values) {
          super.onProgressUpdate(values);
          Log.i("ListXLSExport",
                  "onProgressUpdate(): " + String.valueOf(values[0]));
      }
    }
    

}
