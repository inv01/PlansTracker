package com.example.planstracker;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import android.provider.ContactsContract;
import android.provider.Settings;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;


public class DetailsFragment extends Fragment 
                                implements GooglePlayServicesClient.ConnectionCallbacks,
                                     GooglePlayServicesClient.OnConnectionFailedListener,
                                     LocationListener{
  private View view;
  private EditText edt_email, edt_phone, edtContactName, edt_memo, edt_hours, edt_price;
  private TextView mAddress, date_text;
  private ImageView btnPicture;
  private PlansDbHelper mDbHelper;
  private SQLiteDatabase db;
  private PlanEvent pe;
  
  private LocationClient mLocationClient;
  private Location mCurrentLocation;
  
  private final static int
  CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
  
  OnTaskChangedListener mCallback;

  // Container Activity must implement this interface
  public interface OnTaskChangedListener {
      public void onTaskChanged();
  }
  
  public static DetailsFragment newInstance(int index) {
      DetailsFragment f = new DetailsFragment();
      Bundle args = new Bundle();
      args.putInt("index", index);
      f.setArguments(args);
      return f;
  }
  @Override
  public void onAttach(Activity activity) {
      super.onAttach(activity);
      
      // This makes sure that the container activity has implemented
      // the callback interface. If not, it throws an exception
      try {
          mCallback = (OnTaskChangedListener) activity;
      } catch (ClassCastException e) {
          throw new ClassCastException(activity.toString()
                  + " must implement OnTaskChangedListener");
      }
  }
  public int getEventID() {
      return getArguments().getInt("index", 0);
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater,
          ViewGroup container, Bundle savedInstanceState) {
      if (container == null) {
          return null;
      }
      mDbHelper = new PlansDbHelper(getActivity());
      db = mDbHelper.getWritableDatabase();
      pe = mDbHelper.getPlanEventById(db, getEventID());
      
      view = inflater
        .inflate(R.layout.details_fragment, container, false);
      edtContactName = (EditText) view.findViewById(R.id.edt_name);
      edt_phone = (EditText) view.findViewById(R.id.edt_phone);
      edt_email = (EditText) view.findViewById(R.id.edt_email);
      edt_memo = (EditText) view.findViewById(R.id.edt_memo);
      edt_price = (EditText) view.findViewById(R.id.edt_price);
      edt_hours = (EditText) view.findViewById(R.id.edt_hours);
      mAddress = (TextView) view.findViewById(R.id.address);
      btnPicture = (ImageView) view.findViewById(R.id.btnPicture);
      date_text = (TextView) view.findViewById(R.id.tvDate);
      initViews(pe);
      
      ImageView btnDateTime = (ImageView) view.findViewById(R.id.btnTime);
      btnDateTime.setOnClickListener(new OnClickListener(){
        @Override
        public void onClick(View v) {
          onChangeDate();
        }
      });
      ImageView btnContact = (ImageView) view.findViewById(R.id.btnContact);
      btnContact.setOnClickListener(new OnClickListener(){
        @Override
        public void onClick(View v) {
          onContactClick(v);
        }
      });
      ImageView btnPhone = (ImageView) view.findViewById(R.id.btnPhone);
      btnPhone.setOnClickListener(new OnClickListener(){
        @Override
        public void onClick(View v) {
          String phoneNumber = ((EditText) view.findViewById(R.id.edt_phone)).getText().toString();
          Intent intent = new Intent(Intent.ACTION_CALL);
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          intent.setData(Uri.parse("tel:" + phoneNumber));
          getActivity().getBaseContext().startActivity(intent);
        }
      });
      ImageView btnSave = (ImageView) view.findViewById(R.id.btnSave);
      btnSave.setOnClickListener(new OnClickListener(){
        @Override
        public void onClick(View v) {
            pe.setPerson_name(edtContactName.getText().toString());
            pe.setEmail_address(edt_email.getText().toString());
            pe.setPhone_number(edt_phone.getText().toString());
            pe.setNote(edt_memo.getText().toString());
            pe.setMoney(Double.parseDouble(edt_price.getText().toString()));
            pe.setHours(Double.parseDouble(edt_hours.getText().toString()));
            pe = mDbHelper.savePlanEvent(db, pe);
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.changes_saved),
                    Toast.LENGTH_SHORT).show();
            mCallback.onTaskChanged();
        }
      });
      ImageView btnDelete = (ImageView) view.findViewById(R.id.btnDelete);
      btnDelete.setOnClickListener(new OnClickListener(){
        @Override
        public void onClick(View v) {
            mDbHelper.onRemoveRecord(db, pe.getId());
            pe = new PlanEvent();
            initViews(pe);
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.task_deleted),
                    Toast.LENGTH_SHORT).show();
            mCallback.onTaskChanged();
        }
      });
      ImageView btnRefresh = (ImageView) view.findViewById(R.id.btnRefreshLocation);
      btnRefresh.setOnClickListener(new OnClickListener(){
        @Override
        public void onClick(View v) {
          onRefreshLocationClick(v);
        }
      });
      ImageView btnPicture = (ImageView) view.findViewById(R.id.btnPicture);
      btnPicture.setOnClickListener(new OnClickListener(){
        @Override
        public void onClick(View v) {
          onPictureClick(v);
        }
      });
      
      return view;
  }
  
  public void initViews(PlanEvent p){
      setUIEventDate(pe);
      edtContactName.setText(p.getPerson_name());
      edt_phone.setText(p.getPhone_number());
      edt_email.setText(p.getEmail_address());
      edt_memo.setText(p.getNote());
      edt_price.setText("" + p.getMoney());
      edt_hours.setText("" + p.getHours());
      if (p.getEvent_location().getLoc_str() != ""){
      mAddress.setText(p.getEvent_location().getLoc_str());
      } else mAddress.setText(getResources().getString(R.string.current_location));
      if(p.getPic() == null) return;
      Bitmap bitmap = BitmapFactory.decodeByteArray(p.getPic(), 0, p.getPic().length);
      btnPicture.setImageBitmap(bitmap);
  }
  
  public void onContactClick(View view){
      Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);  
      startActivityForResult(intent, 1);
  }
  
  @Override
  public void onActivityResult(int reqCode, int resultCode, Intent data) {
      super.onActivityResult(reqCode, resultCode, data);
      switch (reqCode) {
          case CONNECTION_FAILURE_RESOLUTION_REQUEST :
          /*
           * If the result code is Activity.RESULT_OK, try
           * to connect again
           */
              switch (resultCode) {
                  case Activity.RESULT_OK :
                    if (servicesConnected()){
                      Toast.makeText(
                          getActivity().getBaseContext(),
                          "Location service is available now", Toast.LENGTH_LONG).show();
                    }
                    break;
              }
      }
      
      
      if (resultCode == Activity.RESULT_OK) {
          Uri contactData = data.getData();
          ContentResolver cr = getActivity().getBaseContext().getContentResolver();
          Cursor cur = cr.query(contactData, null, null, null, null);
          if (cur.moveToFirst()) {
              String id = cur.getString(
                            cur.getColumnIndex(ContactsContract.Contacts._ID));
              String name = cur.getString(
                                  cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
              edtContactName.setText(name);
              //getting first phone number
              if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                  if (Integer.parseInt(cur.getString(
                          cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                       Cursor pCur = cr.query(
                   ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
                   null, 
                   ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
                   new String[]{id}, null);
                     if (pCur.moveToFirst()) {
                         String number = pCur.getString(pCur.
                                 getColumnIndexOrThrow(ContactsContract.
                                         CommonDataKinds.Phone.NUMBER));
                         edt_phone.setText(number);
                     }
                     pCur.close();
                  }
              }
              //getting first email address
              Cursor emailCur = cr.query( 
                      ContactsContract.CommonDataKinds.Email.CONTENT_URI, 
                      null,
                      ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", 
                      new String[]{id}, null); 
                    if (emailCur.moveToFirst()) { 
                        String email = emailCur.getString(
                                        emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        edt_email.setText(email);
                        //String emailType = emailCur.getString(
                        //                emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE)); 
                    } 
                    emailCur.close();
              // END getting first email address
             }
      }  
  }
  
  public void onChangeDate(){
      final Calendar cal = Calendar.getInstance();
      cal.setTime(pe.getDate());
      
      new DatePickerDialog(getActivity(),
              new DatePickerDialog.OnDateSetListener() {
                @Override 
                public void onDateSet(DatePicker view, int y, int m, int d) {
                    cal.set(Calendar.YEAR, y);
                    cal.set(Calendar.MONTH, m);
                    cal.set(Calendar.DAY_OF_MONTH, d);
                    // now show the time picker
                    new TimePickerDialog(getActivity(),
                            new TimePickerDialog.OnTimeSetListener() {
                            @Override 
                            public void onTimeSet(TimePicker view, 
                                    int h, int min) {
                                cal.set(Calendar.HOUR_OF_DAY, h);
                                cal.set(Calendar.MINUTE, min);
                                
                            }
                        
                        }
                    , cal.get(Calendar.HOUR_OF_DAY), 
                      cal.get(Calendar.MINUTE), true).show();
                 }
              }
          , cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
      .show();
      pe.setDate(cal.getTime());
      setUIEventDate(pe);
  }
  
  public void setUIEventDate(PlanEvent p){
      SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
      date_text.setText(sf.format(p.getDate()));
  }
  
  public static boolean isMockLocationSet(Context context) { 
      if (Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).contentEquals("1")) { 
          return true;  
      } 
      else { 
          return false;
      } 
  } 
  
  public void onRefreshLocationClick(View view){
      if (servicesConnected()){
          //ContentResolver cr = getActivity().getBaseContext().getContentResolver();
          if (isMockLocationSet(getActivity().getBaseContext()))
          mLocationClient.setMockMode(true);
          mCurrentLocation = mLocationClient.getLastLocation();
          if (Build.VERSION.SDK_INT >=
              Build.VERSION_CODES.GINGERBREAD
                          &&
              Geocoder.isPresent()) {
          // Show the activity indicator
          //mActivityIndicator.setVisibility(View.VISIBLE);
          
          (new GetAddressTask(getActivity().getBaseContext())).execute(mCurrentLocation);
      }
        }
  }
  
  private boolean servicesConnected() {
      // Check that Google Play services is available
      int resultCode =
              GooglePlayServicesUtil.
                      isGooglePlayServicesAvailable(getActivity());
      if (ConnectionResult.SUCCESS == resultCode) {
          // In debug mode, log the status
          Log.d("Location Updates",
                  "Google Play services is available.");
          return true;
      // Google Play services was not available for some reason
      } else {
          showErrorDialog(resultCode);
          return false;
      }
  }
    private void showErrorDialog(int errorCode){
      Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
          errorCode,
              getActivity(),
              CONNECTION_FAILURE_RESOLUTION_REQUEST);

      // If Google Play services can provide an error dialog
      if (errorDialog != null) {
          ErrorDialogFragment errorFragment =
                  new ErrorDialogFragment();
          errorFragment.setDialog(errorDialog);
          errorFragment.show(((FragmentActivity) getActivity()).getSupportFragmentManager(),
                  "Location Updates");
      }
    }
  //Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
    
  private class GetAddressTask extends
    AsyncTask<Location, Void, String> {
      Context mContext;
      public GetAddressTask(Context context) {
          super();
          mContext = context;
      }
    @Override
    protected void onPostExecute(String address) {
        //mActivityIndicator.setVisibility(View.GONE);
        mAddress.setText(address);
        EventLocation e_loc = pe.getEvent_location();
        e_loc.setLoc_str(address);
        pe.setEvent_location(e_loc);
    }
    @Override
    protected String doInBackground(Location... params) {
      Geocoder geocoder =
              new Geocoder(mContext, Locale.getDefault());
      // Get the current location from the input parameter list
      Location loc = params[0];
      // Create a list to contain the result address
      List<Address> addresses = null;
      try {
          /*
           * Return 1 address.
           */
          addresses = geocoder.getFromLocation(loc.getLatitude(),
                  loc.getLongitude(), 1);
      } catch (IOException e1) {
      Log.e("LocationSampleActivity",
              "IO Exception in getFromLocation()");
      e1.printStackTrace();
      return ("IO Exception trying to get address");
      } catch (IllegalArgumentException e2) {
      // Error message to post in the log
      String errorString = "Illegal arguments " +
              Double.toString(loc.getLatitude()) +
              " , " +
              Double.toString(loc.getLongitude()) +
              " passed to address service";
      Log.e("LocationSampleActivity", errorString);
      e2.printStackTrace();
      return errorString;
      }
      // If the reverse geocode returned an address
      if (addresses != null && addresses.size() > 0) {
          // Get the first address
          Address address = addresses.get(0);
          /*
           * Format the first line of address (if available),
           * city, and country name.
           */
          String addressText = String.format(
                  "%s, %s, %s",
                  // If there's a street address, add it
                  address.getMaxAddressLineIndex() > 0 ?
                          address.getAddressLine(0) : "",
                  // Locality is usually a city
                  address.getLocality(),
                  // The country of the address
                  address.getCountryName());
          // Return the text
          return addressText;
      } else {
          return "No address found";
      }
    }
    }
  
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
      /*
       * Google Play services can resolve some errors it detects.
       * If the error has a resolution, try sending an Intent to
       * start a Google Play services activity that can resolve
       * error.
       */
      if (connectionResult.hasResolution()) {
          try {
              // Start an Activity that tries to resolve the error
              connectionResult.startResolutionForResult(
                      getActivity(),
                      CONNECTION_FAILURE_RESOLUTION_REQUEST);
              /*
               * Thrown if Google Play services canceled the original
               * PendingIntent
               */
          } catch (IntentSender.SendIntentException e) {
              // Log the error
              e.printStackTrace();
          }
      } else {
          /*
           * If no resolution is available, display a dialog to the
           * user with the error.
           */
          showErrorDialog(connectionResult.getErrorCode());
      }
    }

    @Override
    public void onConnected(Bundle arg0) {
      Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisconnected() {
      Toast.makeText(getActivity(), "Disconnected. Please re-connect.",
              Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getActivity().getBaseContext(),
            this, this);
    }

    /*
     * Called when the Activity becomes visible.
     */
    @Override
    public void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    public void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }
  //Define the callback method that receives location updates
    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        EventLocation e_loc = pe.getEvent_location();
        e_loc.setLoc_latitude(location.getLatitude());
        e_loc.setLoc_longitude(location.getLongitude());
        e_loc.setLoc_str("");
        pe.setEvent_location(e_loc);
        
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }
  
  public void onPictureClick(View view){
      
  }
}
