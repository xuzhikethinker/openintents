package org.openintents.samples.TestAboutApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.openintents.intents.AboutIntents;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class ShowAboutWithExtras {
	private static final String TAG = "ShowAboutWithExtras";

	public static void showAboutWithExtras(Activity activity) {

		Intent intent = new Intent(AboutIntents.ACTION_SHOW_ABOUT_DIALOG);

		intent.putExtra(AboutIntents.EXTRA_PACKAGE_NAME, activity.getPackageName());
		
		//Supply the image.
		/*//alternative 2b: Put the image resId into the provider.
		Bitmap image = BitmapFactory.decodeResource(getResources(), 
				R.drawable.icon);//lossy
		String uri = Images.Media.insertImage(getContentResolver(), image,
				getString(R.string.about_logo_title), 
				getString(R.string.about_logo_description));
		intent.putExtra(AboutIntents.EXTRA_LOGO, uri);*/
		
		//alternative 3: Supply the image name and package.
		intent.putExtra(AboutIntents.EXTRA_ICON_RESOURCE, activity.getResources()
				.getResourceName(R.drawable.icon));
		Log.i(TAG, "package for icon: " + activity.getResources()
				.getResourcePackageName(R.drawable.icon));

		intent.putExtra(AboutIntents.EXTRA_APPLICATION_LABEL,
				activity.getString(R.string.app_name));
		
		//Get the app version
		String version = "?";
		try {
		        PackageInfo pi = activity.getPackageManager().getPackageInfo(
		        		activity.getPackageName(), 0);
		        version = pi.versionName;
		} catch (PackageManager.NameNotFoundException e) {
		        Log.e(TAG, "Package name not found", e);
		}
		intent.putExtra(AboutIntents.EXTRA_VERSION_NAME, version);
		
		intent.putExtra(AboutIntents.EXTRA_COMMENTS,
				activity.getString(R.string.about_comments));
		intent.putExtra(AboutIntents.EXTRA_COPYRIGHT,
				activity.getString(R.string.about_copyright));
		intent.putExtra(AboutIntents.EXTRA_WEBSITE_LABEL,
				activity.getString(R.string.about_website_label));
		intent.putExtra(AboutIntents.EXTRA_WEBSITE_URL,
				activity.getString(R.string.about_website_url));
		intent.putExtra(AboutIntents.EXTRA_AUTHORS, activity.getResources()
				.getStringArray(R.array.about_authors));
		intent.putExtra(AboutIntents.EXTRA_DOCUMENTERS, activity.getResources()
				.getStringArray(R.array.about_documenters));
		intent.putExtra(AboutIntents.EXTRA_TRANSLATORS, activity.getResources()
				.getStringArray(R.array.about_translators));
		intent.putExtra(AboutIntents.EXTRA_ARTISTS, activity.getResources()
				.getStringArray(R.array.about_artists));
		
		//Read in the license file as a big String
		BufferedReader in
		   = new BufferedReader(new InputStreamReader(
				   activity.getResources().openRawResource(R.raw.license_short)));
		String license = "";
		String line;
		try {
			while ((line = in.readLine()) != null) { // Read line per line.
				license += line + "\n";
			}
		} catch (IOException e) {
			//Should not happen.
			e.printStackTrace();
		}
		intent.putExtra(AboutIntents.EXTRA_LICENSE, license);
		intent.putExtra(AboutIntents.EXTRA_WRAP_LICENSE, false);
		
		// Start about activity. Needs to be "forResult" with requestCode>=0
		// because the About dialog may call elements from your Manifest by your
		// package name.
		activity.startActivityForResult(intent, 0);
	}
}