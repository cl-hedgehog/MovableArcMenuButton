package com.cmcc.testarcmenu;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.cmcc.arcmenubutton.ArcMenuButtons;

import librarys.develop.bmr.movablearcmenubutton.R;

public class MainActivity extends Activity
{

	public static final String TAG = "TEST";

	private ArcMenuButtons mAnimMenu;
	private RadioButton rdLeft;
	private RadioButton rdMiddle;
	private RadioButton rdRight;
	private RadioGroup rgType;

	private int type = 1;
	private static final int[] ITEM_DRAWABLES =
	{ R.drawable.icon_find, R.drawable.icon_hi, R.drawable.icon_write, R.drawable.icon_prison };

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.d("TAG", "onCreate 0");
		setContentView(R.layout.activity_main);
		Log.d("TAG", "onCreate 1");

		mAnimMenu = (ArcMenuButtons) findViewById(R.id.anim_menu);
		mAnimMenu.setOnButtonClickListener(listener);
		mAnimMenu.setType(2);
		for (int i = 0; i < 4; i++)
		{
			ImageButton item = new ImageButton(this);
			item.setBackgroundResource(ITEM_DRAWABLES[i]);
			item.setTag(i);
			mAnimMenu.addItem(item);
		}

		rgType = (RadioGroup) findViewById(R.id.rg_type);
		rgType.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				switch (checkedId)
				{
				case R.id.rb_left:
					mAnimMenu.setType(0);
					break;
				case R.id.rb_middle:
					mAnimMenu.setType(1);
					break;
				case R.id.rb_right:
					mAnimMenu.setType(2);
					break;
				}
			}
		});
	}

	ArcMenuButtons.OnButtonClickListener listener = new ArcMenuButtons.OnButtonClickListener()
	{
		@Override
		public void onButtonClick(View v, int id)
		{
			mAnimMenu.closeMenu();
			switch ((Integer)v.getTag())
			{
			case 0:
				// find
				Toast.makeText(MainActivity.this, "你点击了find",
						Toast.LENGTH_SHORT).show();
				break;
			case 1:
				// hi
				Toast.makeText(MainActivity.this, "你点击了hi", Toast.LENGTH_SHORT)
						.show();
				break;
			case 2:
				// write
				Toast.makeText(MainActivity.this, "你点击了write",
						Toast.LENGTH_SHORT).show();
				break;
			case 3:
				Toast.makeText(MainActivity.this, "你点击了person",
						Toast.LENGTH_SHORT).show();
				// person
				break;
			}
		}
	};
}
