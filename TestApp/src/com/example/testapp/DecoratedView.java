package com.example.testapp;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.text.Html;
import android.widget.TextView;

/** This class helps create custom TextView with colours, emphasis tags, etc... */
public class DecoratedView {

	public class Tag {
		/** For example 'p' or 'h2' */
		public String name;
		/** If used, please prefix with single space */
		public String attributes;

		public Tag(String _name, String _attributes) {
			name = _name;
			attributes = _attributes;
		}
	}

	private String mHtml = "";

	public void appendText(String msg) {
		mHtml += msg;
	}

	public void appendText(String msg, int colour) {
		Tag t = new Tag("font", " color=\"#" + Integer.toHexString(colour)
				+ "\"");
		ArrayList<Tag> tags = new ArrayList<Tag>();
		tags.add(t);
		appendTextInternal(msg, tags);
	}

	/** Example, names can be passed in as {"strong", "em"} */
	public void appendText(String msg, ArrayList<String> names) {
		ArrayList<Tag> tags = new ArrayList<Tag>();
		for (String s : names) {
			tags.add(new Tag(s, ""));
		}
		appendTextInternal(msg, tags);
	}

	public void appendText(String msg, int colour, ArrayList<String> names) {
		ArrayList<Tag> tags = new ArrayList<Tag>();
		Tag t = new Tag("font", " color=\"#" + Integer.toHexString(colour)
				+ "\"");
		tags.add(t);
		for (String s : names) {
			tags.add(new Tag(s, ""));
		}
		appendTextInternal(msg, tags);
	}

	private void appendTextInternal(String msg, ArrayList<Tag> tags) {
		for (Tag t : tags) {
			mHtml += '<' + t.name + t.attributes + '>';
		}

		mHtml += msg;

		Collections.reverse(tags);
		for (Tag t : tags) {
			mHtml += "</" + t.name + '>';
		}
	}

	public String getHtmlString() {
		return mHtml;
	}

	public TextView getTextView(Context c) {
		TextView tv = new TextView(c);
		tv.setText(Html.fromHtml(mHtml));
		return tv;
	}

}
