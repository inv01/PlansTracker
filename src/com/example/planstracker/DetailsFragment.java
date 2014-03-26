package com.example.planstracker;

import com.example.wokabstar.R;

import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
//import android.widget.ProgressBar;
import android.widget.TextView;

import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;


public class DetailsFragment extends Fragment {
  private String phoneNumber;
  private View view;
  
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
  
  public void setAdapter() {
      //String[] in_words = mDbHelper.getWordsMatchingQuery(db, "");
      //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, in_words);
	  String[] FROM_COLUMNS = {
          Build.VERSION.SDK_INT
                  >= Build.VERSION_CODES.HONEYCOMB ?
                  Contacts.DISPLAY_NAME_PRIMARY :
                  Contacts.DISPLAY_NAME
	  };
	  SimpleCursorAdapter mCursorAdapter = new SimpleCursorAdapter(
              getActivity(),
              R.layout.contact_list_item,
              null,
              FROM_COLUMNS, TO_IDS,
              0);
	  AutoCompleteTextView edtContactName = (AutoCompleteTextView) getActivity().findViewById(R.id.edt_name);
	  edtContactName.setAdapter(mCursorAdapter);
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater,
          ViewGroup container, Bundle savedInstanceState) {
      if (container == null) {
          return null;
      }
      view = inflater
        .inflate(R.layout.details_fragment, container, false);
      LinearLayout details = (LinearLayout) view.findViewById(R.id.date_layout);
      TextView text = new TextView(getActivity());
      int padding = (int)TypedValue.applyDimension(
              TypedValue.COMPLEX_UNIT_DIP,
              4, getActivity().getResources().getDisplayMetrics());
      text.setPadding(padding, padding, padding, padding);
      details.addView(text);
      text.setText("" + getShownIndex());
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
      return view;
  }
  
  public void onContactClick(View view){
    //
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
