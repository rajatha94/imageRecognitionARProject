package com.vuforia.samples.VuforiaSamples.ui.ActivityList;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.vuforia.samples.VuforiaSamples.R;

public class AboutScreen extends AppCompatActivity implements TabLayout.OnTabSelectedListener, frag2.onButtonClickListener {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    ImageView home;
    ImageView help;
    private frag1 fg1;
    private frag2 fg2;
    public int selectedTabIndex;
    private static final String LOGTAG = "AboutScreen";
    private TextView mAboutTextTitle;
    private String mClassToLaunch;
    private String mClassToLaunchPackage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_tabs);

        Bundle extras = getIntent().getExtras();
        mClassToLaunchPackage = getPackageName();
        mClassToLaunch = mClassToLaunchPackage + "."
                + extras.getString("ACTIVITY_TO_LAUNCH");

        fg1 = new frag1();
        fg2 = new frag2();
        home = new ImageView(this);
        home.setImageResource(R.drawable.home);
        help = new ImageView(this);
        help.setImageResource(R.drawable.help);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setCustomView(home));
        tabLayout.addTab(tabLayout.newTab().setCustomView(help));
        tabLayout.addOnTabSelectedListener(this);
        tabLayout.getTabAt(0).select();
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
    }

    public void onTabSelected(TabLayout.Tab tab)
    {
        selectedTabIndex = tab.getPosition();
        setCurrentTabFragment(tab.getPosition());
        View focus = getCurrentFocus();
    }

    public void onTabUnselected(TabLayout.Tab tab)
    {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        setCurrentTabFragment(tab.getPosition());
    }

    private void setCurrentTabFragment(int tabPosition){

        switch (tabPosition)
        {
            case 0 :
                replaceFragment(fg2);
                break;
            case 1 :
                replaceFragment(fg1);
                break;
        }
    }
    private void replaceFragment(Fragment fragmentToBeReplaced){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frame_container, fragmentToBeReplaced);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();

    }

    private void startARActivity()
    {
        String mClassToLaunch;
        String mClassToLaunchPackage;
        mClassToLaunchPackage = getPackageName();
        mClassToLaunch = mClassToLaunchPackage + "." + "app.ImageTargets.ImageTargets";
        Intent i = new Intent();
        i.setClassName(mClassToLaunchPackage, mClassToLaunch);
        startActivity(i);
    }

    private void startImageRecoActivity()
    {
        String mClassToLaunch;
        String mClassToLaunchPackage;
        mClassToLaunchPackage = getPackageName();
        mClassToLaunch = mClassToLaunchPackage + "." + "app.ImageTargets.ImageTargets";
        Intent i = new Intent();
        i.setClassName(mClassToLaunchPackage, mClassToLaunch);
        startActivity(i);
    }

    @Override
    public void onButtonClicked(View view)
    {
        switch (view.getId())
        {
            case R.id.button_start:
                startARActivity();
                break;
        }
    }

    @Override
    public void onImageButtonClicked(View view){

        switch (view.getId())
        {
            case R.id.button_start1:
                startImageRecoActivity();
                break;
        }
    }
}
