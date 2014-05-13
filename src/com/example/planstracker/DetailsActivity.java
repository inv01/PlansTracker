package com.example.planstracker;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class DetailsActivity extends FragmentActivity implements DetailsFragment.OnTaskChangedListener{
  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (getResources().getConfiguration().orientation
              == Configuration.ORIENTATION_LANDSCAPE) {
          // If the screen is now in landscape mode, we can show the
          // dialog in-line so we don't need this activity.
          finish();
          return;
      }

      if (savedInstanceState == null) {
          // During initial setup, plug in the details fragment.
          DetailsFragment details = new DetailsFragment();
          details.setArguments(getIntent().getExtras());
          getSupportFragmentManager().beginTransaction().add(
                  android.R.id.content, details).commit();
      }
  }

@Override
public void onTaskChanged() {}
}
