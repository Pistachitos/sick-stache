/*
 * 	SickStache is a android application for managing SickBeard
 * 	Copyright (C) 2012  David Stocking dmstocking@gmail.com
 * 
 * 	http://code.google.com/p/sick-stashe/
 * 	
 * 	SickStache is free software: you can redistribute it and/or modify
 * 	it under the terms of the GNU General Public License as published by
 * 	the Free Software Foundation, either version 3 of the License, or
 * 	(at your option) any later version.
 * 	
 * 	This program is distributed in the hope that it will be useful,
 * 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 * 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * 	GNU General Public License for more details.
 * 	
 * 	You should have received a copy of the GNU General Public License
 * 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sickstache;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TitlePageIndicator;
import org.sickstache.dialogs.WhatsNewDialog;
import org.sickstache.fragments.FutureFragment;
import org.sickstache.fragments.HistoryFragment;
import org.sickstache.fragments.ShowsFragment;
import org.sickstache.helper.BannerCache;
import org.sickstache.helper.Preferences;

public class HomeActivity extends SherlockFragmentActivity implements OnSharedPreferenceChangeListener {
	
	public static SharedPreferences pref;
	
	private static int PREFRENCES_ACTIVITY_REQUEST_CODE = 1;
	
	private boolean preferencesChanged = false;
	
	private ViewPager viewpager;
	private SlideAdapter pageAdapter;
	private TitlePageIndicator pageIndicator;
	
	private ShowsFragment showFrag;
	private FutureFragment futureFrag;
	private HistoryFragment historyFrag;
	
	private PendingIntent notificationPendingIntent;
	
//	private PingChecker pinger;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // doing this just so that its initialized even though its a lazy initializer
        Preferences.getSingleton(this).registerSharedPreferencesChangedListener(this);
        BannerCache.getSingleton(this);
        
        updateNotificationService();

        setContentView(R.layout.main);
        showFrag = new ShowsFragment();
        futureFrag = new FutureFragment();
        historyFrag = new HistoryFragment();
        
        viewpager = ((ViewPager)findViewById(R.id.viewpager));
        pageIndicator = ((TitlePageIndicator)findViewById(R.id.viewPagerIndicator));
        pageAdapter =  new SlideAdapter( this.getSupportFragmentManager() );
        viewpager.setAdapter( pageAdapter );
        pageIndicator.setViewPager( viewpager );
        
        if ( Preferences.getSingleton(this).isUpdated ) {
        	showWhatsNewDiag();
        }
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	if(item.getItemId() == R.id.settingsMenuItem)
    	{
    		Intent intent = new Intent( this, PreferencesActivity.class );
    		this.startActivityForResult(intent, PREFRENCES_ACTIVITY_REQUEST_CODE);
    		return true;
    	}
		// MOVED TO SETTINGS!!!!
//    	case R.id.cacheMenuItem:
//    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//			builder.setMessage(R.string.cache_delete)
//			       .setCancelable(false)
//			       .setPositiveButton( R.string.yes , new DialogInterface.OnClickListener() {
//			           public void onClick(DialogInterface dialog, int id) {
//			        	   // all caches are now in there own directory so this is to clear the data for all the old users
//			        	   for ( File f : getExternalCacheDir().listFiles() ) {
//			        		   if ( f.isFile() == true ) {
//			        			   f.delete();
//			        		   }
//			        	   }
//			        	   if ( HomeActivity.this != null )
//			        		   BannerCache.getSingleton(HomeActivity.this).clear();
//			           }
//			       }).setNegativeButton( R.string.no , new DialogInterface.OnClickListener() {
//			           public void onClick(DialogInterface dialog, int id) {
//			        	   ; // do nothing
//			           }
//			       });
//			AlertDialog alert = builder.create();
//			alert.show();
//    		return true;
    		// this was moved to a tab on the home activity
//    	case R.id.historyMenuItem:
//    		Intent historyIntent = new Intent( this, HistoryActivity.class );
//    		this.startActivity(historyIntent);
//    		return true;
    	else if(item.getItemId() == R.id.logMenuItem)
    	{
    		Intent logIntent = new Intent( this, LogActivity.class );
    		this.startActivity(logIntent);
    		return true;
    	}
    	else if(item.getItemId() == R.id.whatsNewMenuItem)
    	{
	    	showWhatsNewDiag();
	        return true;
    	}
    	else if(item.getItemId() == R.id.helpMenuItem)
    	{
			Intent i = new Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/Buttink/sick-stache/wiki/FAQ"));
			startActivity(i);
			return true;
    	}
    	else if(item.getItemId() == R.id.aboutMenuItem)
    	{
    		Intent aboutIntent = new Intent( this, AboutActivity.class );
    		this.startActivity(aboutIntent);
    		return true;
    	}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// create the menu button options NOT ACTIONBAR!
		this.getSupportMenuInflater().inflate(R.menu.home_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// if we came back from the PreferencesActivity
		if ( requestCode == PREFRENCES_ACTIVITY_REQUEST_CODE ) {
			if ( preferencesChanged ) {
				showFrag.refresh();
				futureFrag.refresh();
				historyFrag.refresh();
				updateNotificationService();
				preferencesChanged = false;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		preferencesChanged = true;
	}

	private void showWhatsNewDiag() {
		// make sure the dialog box isnt already up
		Fragment f = getSupportFragmentManager().findFragmentByTag("whatsnew");
		if ( f == null ) {
			// since it isnt lets make it
		    WhatsNewDialog diag = new WhatsNewDialog();
		    diag.setOnOkClick( new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Preferences.getSingleton(HomeActivity.this).isUpdated = false;
				}
			});
		    diag.show(getSupportFragmentManager(), "whatsnew");
		}
	}

	private void updateNotificationService() {
//        // this pending intent SHOULD be the same intent no matter what happens
//        notificationPendingIntent = PendingIntent.getService(this, 0, new Intent(this, NotificationService.class), PendingIntent.FLAG_UPDATE_CURRENT);
//        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
//		if ( Preferences.singleton.getHistoryService() == true ) {
////	        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, AlarmManager.INTERVAL_HOUR, notificationPendingIntent);
//	        am.setInexactRepeating(AlarmManager.RTC, 0, 1000*15, notificationPendingIntent); // 15 seconds ONLY FOR TESTING
//        } else {
//        	am.cancel(notificationPendingIntent);
//        }
	}

	private class SlideAdapter extends FragmentPagerAdapter {

		public SlideAdapter(FragmentManager fm) {
			super(fm);
		}
		
		@Override
		public Fragment getItem(int arg0) {
			switch( arg0 ) {
			case 0:
				return showFrag;
			case 1:
				return futureFrag;
			case 2:
				return historyFrag;
			}
			return null;
		}

		@Override
		public int getCount() {
			return 3;
		}

		public String getPageTitle(int position) {
			switch( position ) {
			case 0:
				return "Shows";
			case 1:
				return "Future Episodes";
			case 2:
				return "History";
			}
			return null;
		}
    }
}
