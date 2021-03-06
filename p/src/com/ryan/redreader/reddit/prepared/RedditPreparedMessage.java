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

package com.ryan.redreader.reddit.prepared;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableStringBuilder;
import android.view.ViewGroup;
import com.laurencedawson.activetextview.ActiveTextView;
import com.ryan.redreader.common.BetterSSB;
import com.ryan.redreader.common.LinkHandler;
import com.ryan.redreader.common.RRTime;
import com.ryan.redreader.reddit.RedditPreparedInboxItem;
import com.ryan.redreader.reddit.things.RedditMessage;

import org.apache.commons.lang3.StringEscapeUtils;
import com.ryan.redreader.R;

import java.util.HashSet;

public final class RedditPreparedMessage implements RedditPreparedInboxItem {

	public SpannableStringBuilder header;
	public final RedditCommentTextParser.ViewGenerator body;
	public final String idAndType;
	public final RedditMessage src;

	private final int rrCommentHeaderBoldCol, rrCommentHeaderAuthorCol;

	public RedditPreparedMessage(final Context context, final RedditMessage message, final long timestamp) {

		this.src = message;

		// TODO custom time

		final TypedArray appearance = context.obtainStyledAttributes(new int[]{
				R.attr.rrCommentHeaderBoldCol,
				R.attr.rrCommentHeaderAuthorCol,
		});

		rrCommentHeaderBoldCol = appearance.getColor(0, 255);
		rrCommentHeaderAuthorCol = appearance.getColor(1, 255);

		body = RedditCommentTextParser.parse(StringEscapeUtils.unescapeHtml4(message.body));

		idAndType = message.name;

		final BetterSSB sb = new BetterSSB();

		if(src.author == null) {
			sb.append("[" + context.getString(R.string.general_unknown) + "]", BetterSSB.FOREGROUND_COLOR | BetterSSB.BOLD, rrCommentHeaderAuthorCol, 0, 1f);
		} else {
			sb.append(src.author, BetterSSB.FOREGROUND_COLOR | BetterSSB.BOLD, rrCommentHeaderAuthorCol, 0, 1f);
		}

		sb.append("   ", 0);
		sb.append(RRTime.formatDurationMsAgo(context, RRTime.utcCurrentTimeMillis() - src.created_utc * 1000L), BetterSSB.FOREGROUND_COLOR | BetterSSB.BOLD, rrCommentHeaderBoldCol, 0, 1f);

		header = sb.get();
	}

	public HashSet<String> computeAllLinks() {
		return LinkHandler.computeAllLinks(StringEscapeUtils.unescapeHtml4(src.body_html));
	}

	public SpannableStringBuilder getHeader() {
		return header;
	}

	public ViewGroup getBody(Context context, float textSize, Integer textCol, ActiveTextView.OnLinkClickedListener listener) {
		return body.generate(context, textSize, textCol, listener, this);
	}
}
