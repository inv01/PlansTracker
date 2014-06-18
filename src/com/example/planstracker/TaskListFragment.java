package com.example.planstracker;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TaskListFragment extends ListFragment {
  boolean mDualPane;
  private PlansDbHelper mDbHelper;
  private Context mContext;
  int mCurCheckPosition = 0;
  private SparseArray<String> list_values;
  
  private static class EventsArrayAdapter extends ArrayAdapter<Object> {
      private final Context context;
      private final SparseArray<String> values;

      public EventsArrayAdapter(Context context, SparseArray<String> values) {
        super(context, android.R.layout.simple_list_item_1, new String[values.size()]);
        this.context = context;
        this.values = values;
      }
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        TextView textView = (TextView) rowView.findViewById(android.R.id.text1);
        textView.setText(values.get(values.keyAt(position)));
        return rowView;
      }
    } 
  
  public void setAdapter(){
      mDbHelper = new PlansDbHelper(getActivity());
      if (getActivity() == null ||
              (mDbHelper == null && mContext != null)){
          mDbHelper = new PlansDbHelper(mContext);
      }
      SQLiteDatabase db = mDbHelper.getWritableDatabase();
      list_values = mDbHelper.getAllTasks(db);
      db.close();
      EventsArrayAdapter adapter = new EventsArrayAdapter(getActivity(), list_values);
      setListAdapter(adapter);
  }

  public void onStart(){
      super.onStart();
      setAdapter();
  }

  public void onAttach(Activity activity){
      super.onAttach(activity);
      this.mContext = activity;
  }
  
  @Override
  public void onActivityCreated(Bundle savedState) {
      super.onActivityCreated(savedState);
      mContext = getActivity();
      View detailsFrame = getActivity().findViewById(R.id.details);
      mDualPane = detailsFrame != null
              && detailsFrame.getVisibility() == View.VISIBLE;

      if (savedState != null) {
          mCurCheckPosition = savedState.getInt("curChoice", 0);
      }
      
      if (mDualPane) {
          setAdapter();
          getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
          showDetails(mCurCheckPosition);
      } else {
          DetailsFragment details = (DetailsFragment)
                  getFragmentManager().findFragmentById(R.id.details);
          if (details != null){
              FragmentTransaction ft = getFragmentManager().beginTransaction();
              ft.remove(details);
              ft.commit();
          }
      }
  }
  
  @Override
  public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      outState.putInt("curChoice", mCurCheckPosition);
  }

  @Override
  public void onListItemClick(ListView l, View v, int pos, long id) {
      showDetails(pos);
  }

  /**
   * Helper function to show the details of a selected item, either by
   * displaying a fragment in-place in the current UI, or starting a
   * whole new activity in which it is displayed.
   */
  void showDetails(int index) {
      mCurCheckPosition = index;

      if (mDualPane) {
           getListView().setItemChecked(index, true);
           
          DetailsFragment details = (DetailsFragment)
                  getFragmentManager().findFragmentById(R.id.details);
          if (details == null || details.getEventID() != index) {
              details = DetailsFragment.newInstance(list_values.keyAt(index));

              FragmentTransaction ft
                      = getFragmentManager().beginTransaction();
              ft.replace(R.id.details, details);
              ft.setTransition(
                      FragmentTransaction.TRANSIT_FRAGMENT_FADE);
              ft.commit();
          }

      } else {
          Intent intent = new Intent();
          intent.setClass(getActivity(), DetailsActivity.class);
          intent.putExtra("index", list_values.keyAt(index));
          startActivity(intent);
      }
  }
}
