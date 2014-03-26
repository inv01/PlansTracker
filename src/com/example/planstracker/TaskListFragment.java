package com.example.planstracker;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TaskListFragment extends ListFragment {
  boolean mDualPane;
  int mCurCheckPosition = 0;

  @Override
  public void onActivityCreated(Bundle savedState) {
      super.onActivityCreated(savedState);
      String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
          "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
          "Linux", "OS/2" };
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
          android.R.layout.simple_list_item_1, values);
      setListAdapter(adapter);
       View detailsFrame = getActivity().findViewById(R.id.details);
      mDualPane = detailsFrame != null
              && detailsFrame.getVisibility() == View.VISIBLE;

      if (savedState != null) {
          mCurCheckPosition = savedState.getInt("curChoice", 0);
      }

      if (mDualPane) {
          getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
          showDetails(mCurCheckPosition);
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
          if (details == null || details.getShownIndex() != index) {
              details = DetailsFragment.newInstance(index);

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
          intent.putExtra("index", index);
          startActivity(intent);
      }
  }
}
