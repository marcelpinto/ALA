package com.hardsoft.alarm.location.adviser;

import java.util.List;
import java.util.Stack;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

public class HelpManager extends SherlockActivity implements OnClickListener {

	private static final int TIPS 		= 0;
	private static final int FAQ 		= 1;
	private static final int CONTACT 	= 2;
	
	private boolean isFAQ = false;
	private boolean isContact = false;
	private ActionBar aBar;

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		
		if (isFAQ || isContact) {
			isFAQ = false;
			isContact = false;
			showHelpManager();
		}
		else {
			super.onBackPressed();
			finish();
		}
	}

	@Override
	protected void onCreate(Bundle bundle) {
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		showHelpManager();
	}
	
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.activity_help, menu);
		return super.onCreateOptionsMenu(menu);
	}

	private void showHelpManager() {
		// TODO Auto-generated method stub
		setContentView(R.layout.help_manager_view);
		aBar = getSupportActionBar();
        aBar.setDisplayShowTitleEnabled(false);
        aBar.setHomeButtonEnabled(true);
        aBar.setDisplayHomeAsUpEnabled(true);
		LinearLayout l1 = (LinearLayout) findViewById(R.id.tip_layout);
		LinearLayout l2 = (LinearLayout) findViewById(R.id.faq_layout);
		LinearLayout l3 = (LinearLayout) findViewById(R.id.contact_layout);
		ImageButton img1 = (ImageButton) findViewById(R.id.img_tips);
		ImageButton img2 = (ImageButton) findViewById(R.id.img_faq);
		ImageButton img3 = (ImageButton) findViewById(R.id.img_contact);
		l1.setTag(TIPS);
		l1.setOnClickListener(this);
		l2.setTag(FAQ);
		l2.setOnClickListener(this);
		l3.setTag(CONTACT);
		l3.setOnClickListener(this);
		img1.setTag(TIPS);
		img2.setTag(FAQ);
		img3.setTag(CONTACT);
		img1.setOnClickListener(this);
		img2.setOnClickListener(this);
		img3.setOnClickListener(this);
	}

	private void showContact() {
		// TODO Auto-generated method stub
		setContentView(R.layout.contact_view);
		isContact= true;
		final EditText name = (EditText) this.findViewById(R.id.name_box);
		final EditText email = (EditText) this.findViewById(R.id.email_box);
		final EditText text = (EditText) this.findViewById(R.id.txt_box);
		Button send = (Button) findViewById(R.id.send_button);
		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (name.getText().toString().equals("") || email.getText().toString().equals("") || text.getText().toString().equals("")) {
					Toast.makeText(getApplicationContext(), R.string.error_text, Toast.LENGTH_SHORT).show();
					return;
				}
				/*Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"skimarxall@gmail.com"});
				i.putExtra(Intent.EXTRA_SUBJECT, "ALA CONTACT");
				i.putExtra(Intent.EXTRA_TEXT   , "NAME: "+name.getText().toString()+"\n"
						+ "EMAIL: "+ email.getText().toString()+ "\nQuestion:\n"+text.getText().toString());
				try {
				    startActivity(Intent.createChooser(i, "Send mail..."));
				} catch (android.content.ActivityNotFoundException ex) {
				    Toast.makeText(HelpManager.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
				}*/
				Intent i = new Intent(Intent.ACTION_SEND);
		        i.setType("*/*"); 
		        i.putExtra(Intent.EXTRA_EMAIL, new String[] {
		            "skimarxall@gmail.com"
		        });
		        i.putExtra(Intent.EXTRA_SUBJECT, "ALA contact");
		        i.putExtra(Intent.EXTRA_TEXT, "NAME: "+name.getText().toString()+"\n"
						+ "EMAIL: "+ email.getText().toString()+ "\nQuestion:\n"+text.getText().toString());

		        startActivity(createEmailOnlyChooserIntent(i, "Send via email"));
			}
			
		});
	}

	private void showFAQ() {
		// TODO Auto-generated method stub
		setContentView(R.layout.faq_view);
		isFAQ = true;
		
	}

	private void showTips() {
		// TODO Auto-generated method stub
		startActivity(new Intent(this, Tips.class));
		finish();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		int rol = (Integer) arg0.getTag();
		switch(rol) {
		case TIPS:
			showTips();
			break;
		case FAQ:
			showFAQ();
			break;
		case CONTACT:
			showContact();
			break;
		}
	}
	
	
	public Intent createEmailOnlyChooserIntent(Intent source,
			CharSequence chooserTitle) {
		Stack<Intent> intents = new Stack<Intent>();
		Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",
				"info@domain.com", null));
		List<ResolveInfo> activities = getPackageManager()
				.queryIntentActivities(i, 0);

		for (ResolveInfo ri : activities) {
			Intent target = new Intent(source);
			target.setPackage(ri.activityInfo.packageName);
			intents.add(target);
		}

		if (!intents.isEmpty()) {
			Intent chooserIntent = Intent.createChooser(intents.remove(0),
					chooserTitle);
			chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
					intents.toArray(new Parcelable[intents.size()]));

			return chooserIntent;
		} else {
			return Intent.createChooser(source, chooserTitle);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId()==android.R.id.home) {
			if (isFAQ || isContact) {
				isFAQ = false;
				isContact = false;
				showHelpManager();
			}
			else {
				finish();
			}
		}
		return super.onOptionsItemSelected(item);
		
	}

}
