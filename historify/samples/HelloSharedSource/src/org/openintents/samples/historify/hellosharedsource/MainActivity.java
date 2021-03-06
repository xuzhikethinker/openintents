/* 
 * Copyright (C) 2011 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openintents.samples.historify.hellosharedsource;

import android.app.Activity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		//last word is a link
		TextView txtMore = (TextView)findViewById(R.id.main_txtMore);
		
		int startPos = txtMore.getText().toString().lastIndexOf(' ');		
		((Spannable)txtMore.getText()).setSpan(new ClickableSpan() {
			@Override
			public void onClick(View widget) {}
		}, startPos+1, txtMore.getText().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

}