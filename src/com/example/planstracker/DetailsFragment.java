package com.example.planstracker;

import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.provider.ContactsContract;

public class DetailsFragment extends Fragment {
  private String phoneNumber;
  private View view;
  private EditText edt_email, edt_phone, edtContactName;
  
  public static DetailsFragment newInstance(int index) {
      DetailsFragment f = new DetailsFragment();
      Bundle args = new Bundle();
      args.putInt("index", index);
      f.setArguments(args);
      return f;
  }

  public int getShownIndex() {
      return getArguments().getInt("index", 0);
  }

 /* @SuppressWarnings("deprecation")
  public void setAdapter() {
      String[] FROM_COLUMNS = {
          Contacts._ID,
          Build.VERSION.SDK_INT
                  >= Build.VERSION_CODES.HONEYCOMB ?
                  Contacts.DISPLAY_NAME_PRIMARY :
                  Contacts.DISPLAY_NAME
        };
        CursorLoader c = new CursorLoader(getActivity(), 
                ContactsContract.Contacts.CONTENT_URI, FROM_COLUMNS, 
                null, null, null);
        //mActivity, null, FROM_COLUMNS, phoneNumber, FROM_COLUMNS, phoneNumber);
        Cursor cursor = c.loadInBackground();
        SimpleCursorAdapter mCursorAdapter = new SimpleCursorAdapter(
              getActivity().getBaseContext(),
              0,
              cursor,
              FROM_COLUMNS, 
              null);
    AutoCompleteTextView edtContactName = (AutoCompleteTextView) view.findViewById(R.id.edt_name);
    edtContactName.setAdapter(mCursorAdapter);
  }*/

  
  
  @Override
  public View onCreateView(LayoutInflater inflater,
          ViewGroup container, Bundle savedInstanceState) {
      if (container == null) {
          return null;
      }
      
      view = inflater
        .inflate(R.layout.details_fragment, container, false);
      edtContactName = (EditText) view.findViewById(R.id.edt_name);
      edt_phone = (EditText) view.findViewById(R.id.edt_phone);
      edt_email = (EditText) view.findViewById(R.id.edt_email);
      //show date of event
      LinearLayout details = (LinearLayout) view.findViewById(R.id.date_layout);
      TextView text = new TextView(getActivity());
      int padding = (int)TypedValue.applyDimension(
              TypedValue.COMPLEX_UNIT_DIP,
              4, getActivity().getResources().getDisplayMetrics());
      text.setPadding(padding, padding, padding, padding);
      details.addView(text);
      text.setText("" + getShownIndex());
      
      //setAdapter();
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
          phoneNumber = ((EditText) view.findViewById(R.id.edt_phone)).getText().toString();
          Intent intent = new Intent(Intent.ACTION_CALL);
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          intent.setData(Uri.parse("tel:" + phoneNumber));
          getActivity().getBaseContext().startActivity(intent);
        }
      });
      ImageView btnEdit = (ImageView) view.findViewById(R.id.btnEdit);
      btnEdit.setOnClickListener(new OnClickListener(){
        @Override
        public void onClick(View v) {
          onEditClick(v);
        }
      });
      ImageView btnSave = (ImageView) view.findViewById(R.id.btnSave);
      btnSave.setOnClickListener(new OnClickListener(){
        @Override
        public void onClick(View v) {
          onSaveClick(v);
        }
      });
      ImageView btnDelete = (ImageView) view.findViewById(R.id.btnDelete);
      btnDelete.setOnClickListener(new OnClickListener(){
        @Override
        public void onClick(View v) {
          onDeleteClick(v);
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
      
      TextView address = (TextView) view.findViewById(R.id.address);
      address.setText("Austria, Klagenfurt; FloriangroegerStrasse 2");
      return view;
  }
  
  public void onContactClick(View view){
      Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);  
      startActivityForResult(intent, 1);
  }
  
  @Override
  public void onActivityResult(int reqCode, int resultCode, Intent data) {
      super.onActivityResult(reqCode, resultCode, data);
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
                        // This would allow you get several email addresses
                              // if the email addresses were stored in an array
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
  
  public void onEditClick(View view){
    
  }
  public void onSaveClick(View view){
    
  }
  public void onDeleteClick(View view){
    
  }
  public void onRefreshLocationClick(View view){
  }
  public void onPictureClick(View view){
    
  }
}
