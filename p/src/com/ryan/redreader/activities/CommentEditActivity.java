/*******************************************************************************
 * This file is part of RedReader.
 *
 * RedReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RedReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RedReader.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

/*******************************************************************************
 * This file is part of RedReader.
 *
 * RedReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RedReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RedReader.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package com.ryan.redreader.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.widget.ScrollView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.ryan.redreader.account.RedditAccount;
import com.ryan.redreader.account.RedditAccountManager;
import com.ryan.redreader.cache.CacheManager;
import com.ryan.redreader.cache.RequestFailureType;
import com.ryan.redreader.common.General;
import com.ryan.redreader.common.PrefsUtility;
import com.ryan.redreader.common.RRError;
import com.ryan.redreader.fragments.MarkdownPreviewDialog;
import com.ryan.redreader.reddit.APIResponseHandler;
import com.ryan.redreader.reddit.RedditAPI;

import org.apache.http.StatusLine;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.ProgressDialog;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.LinearLayout;
import com.ryan.redreader.R;

public class CommentEditActivity extends Activity {

	private EditText textEdit;

	private String commentIdAndType = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		PrefsUtility.applyTheme(this);

		super.onCreate(savedInstanceState);

		final LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.comment_edit);

		textEdit = (EditText)layout.findViewById(R.id.comment_reply_text);

		if(getIntent() != null && getIntent().hasExtra("commentIdAndType")) {
			commentIdAndType = getIntent().getStringExtra("commentIdAndType");
			textEdit.setText(getIntent().getStringExtra("commentText"));

		} else if(savedInstanceState != null && savedInstanceState.containsKey("commentIdAndType")) {
			textEdit.setText(savedInstanceState.getString("commentText"));
			commentIdAndType = savedInstanceState.getString("commentIdAndType");
		}

		final ScrollView sv = new ScrollView(this);
		sv.addView(layout);
		setContentView(sv);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("commentText", textEdit.getText().toString());
		outState.putString("commentIdAndType", commentIdAndType);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		final MenuItem send = menu.add(R.string.comment_edit_save);
		send.setIcon(R.drawable.ic_action_save_dark);
		send.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		menu.add(R.string.comment_reply_preview);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if(item.getTitle().equals(getString(R.string.comment_edit_save))) {

			final ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setTitle(getString(R.string.comment_reply_submitting_title));
			progressDialog.setMessage(getString(R.string.comment_reply_submitting_message));
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(true);
			progressDialog.setCanceledOnTouchOutside(false);

			progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(final DialogInterface dialogInterface) {
					General.quickToast(CommentEditActivity.this, R.string.comment_reply_oncancel);
					progressDialog.dismiss();
				}
			});

			progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
				public boolean onKey(final DialogInterface dialogInterface, final int keyCode, final KeyEvent keyEvent) {

					if(keyCode == KeyEvent.KEYCODE_BACK) {
						General.quickToast(CommentEditActivity.this, R.string.comment_reply_oncancel);
						progressDialog.dismiss();
					}

					return true;
				}
			});

			final APIResponseHandler.ActionResponseHandler handler = new APIResponseHandler.ActionResponseHandler(this) {
				@Override
				protected void onSuccess() {
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						public void run() {
							if(progressDialog.isShowing()) progressDialog.dismiss();
							General.quickToast(CommentEditActivity.this, R.string.comment_edit_done);
							finish();
						}
					});
				}

				@Override
				protected void onCallbackException(Throwable t) {
					BugReportActivity.handleGlobalError(CommentEditActivity.this, t);
				}

				@Override
				protected void onFailure(RequestFailureType type, Throwable t, StatusLine status, String readableMessage) {

					final RRError error = General.getGeneralErrorForFailure(context, type, t, status);

					new Handler(Looper.getMainLooper()).post(new Runnable() {
						public void run() {
							General.showResultDialog(CommentEditActivity.this, error);
							if(progressDialog.isShowing()) progressDialog.dismiss();
						}
					});
				}

				@Override
				protected void onFailure(final APIFailureType type) {

					final RRError error = General.getGeneralErrorForFailure(context, type);

					new Handler(Looper.getMainLooper()).post(new Runnable() {
						public void run() {
							General.showResultDialog(CommentEditActivity.this, error);
							if(progressDialog.isShowing()) progressDialog.dismiss();
						}
					});
				}
			};

			final CacheManager cm = CacheManager.getInstance(this);
			final RedditAccount selectedAccount = RedditAccountManager.getInstance(this).getDefaultAccount();

			RedditAPI.editComment(cm, handler, selectedAccount, commentIdAndType, textEdit.getText().toString(), this);

			progressDialog.show();

		} else if(item.getTitle().equals(getString(R.string.comment_reply_preview))) {
			MarkdownPreviewDialog.newInstance(textEdit.getText().toString()).show(getSupportFragmentManager());
		}

		return true;
	}
}
