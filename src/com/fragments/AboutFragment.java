package com.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.peerblock.R;

public class AboutFragment extends Activity
{
	public AboutFragment()
	{
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_peer_block_dummy);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		TextView aboutTextView = (TextView)findViewById(R.id.section_label);
		aboutTextView.setMovementMethod(new ScrollingMovementMethod());
		aboutTextView.setText("This program is made by DragonHunter, This is somewhat a port from the computer version to Android\r\n" +
							  "Keep in mind that I'm not a member of the PeerBlock team, I just wanted PeerBlock for phone\r\n" +
							  "You're able to grab the lists from iblocklist.com so you can start blocking those evil hosts\r\n" +
							  "To add lists to PeerBlock create a new directory in the root of the sdcard (not external sdcard)\r\n" +
							  "Called 'PeerBlockLists' here should be all the text files \r\n" +
							  "Everytime you added a new/updated list to your PeerBlockLists please press the 'Rebuild cache blocklist' and reboot so that new hosts can be blocked" +
							  "\r\n\r\nPeerBlock lets you control who your phone 'talks to' on the Internet.\r\nBy selecting appropriate lists of 'known bad' computers, you can block communication with advertising or spyware oriented servers,\r\ncomputers monitoring your p2p activities, computers which have been 'hacked', even entire countries!\r\nThey can't get in to your phone, and your phone won't try to send them anything either.\r\n\r\nAnd best of all, it's free!");
	}
}