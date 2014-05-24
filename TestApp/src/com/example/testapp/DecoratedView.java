package com.example.testapp;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.graphics.Color;
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

		public Tag(String _name) {
			this(_name, "");
		}
	}

	private String mHtml = "";

	/**
	 * Note: the message is not escaped, so the caller must escape it. Not
	 * escaping allows the user to create their own tags manually.
	 */
	public void appendText(String msg) {
		mHtml += msg;
	}

	public void appendText(String msg, int colour) {
		Tag t = new Tag("font", " color=\"#" + String.format("%06x", colour)
				+ "\"");
		ArrayList<Tag> tags = new ArrayList<Tag>();
		tags.add(t);
		appendTextInternal(msg, tags);
	}

	/** Example, names can be passed in as {"strong", "em"} */
	public void appendText(String msg, ArrayList<String> names) {
		ArrayList<Tag> tags = new ArrayList<Tag>();
		for (String s : names) {
			tags.add(new Tag(s));
		}
		appendTextInternal(msg, tags);
	}

	public void appendText(String msg, int colour, ArrayList<String> names) {
		ArrayList<Tag> tags = new ArrayList<Tag>();
		Tag t = new Tag("font", " color=\"#" + String.format("%06x", colour)
				+ "\"");
		tags.add(t);
		for (String s : names) {
			tags.add(new Tag(s));
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
		return "<p>" + mHtml + "</p>";
	}

	public TextView getTextView(Context c) {
		TextView tv = new TextView(c);
		tv.setText(Html.fromHtml(getHtmlString()));
		return tv;
	}

	/**
	 * This is an example of how to use this class, although this code may not
	 * run in an Android environment.
	 * 
	 * @param args
	 *            unused
	 */
	public static void main(String[] args) {
		DecoratedView dv = new DecoratedView();

		dv.appendText("hello world");
		dv.appendText("the sky is blue", (Color.BLUE));
		ArrayList<String> names = new ArrayList<String>();
		names.add("strong");
		dv.appendText("the sun is red and strong", Color.RED, names);

		System.out.println(dv.getHtmlString());
	}
}
