package com.branch.techchallenge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.SharingHelper;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;
import io.branch.referral.util.ShareSheetStyle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.json.JSONObject;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.share_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareDeepLink();
            }
        });
    }

    /**
     * Initialize Branch in onStart() as recommended.
     */
    @Override public void onStart() {
        super.onStart();
        Branch.sessionBuilder(this)
                .withCallback(branchReferralInitListener)
                .withData(getIntent() != null ? getIntent().getData() : null)
                .init();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // if activity is in foreground (or in backstack but partially visible) launching the same
        // activity will skip onStart, handle this case with reInitSession
        Branch.sessionBuilder(this).withCallback(branchReferralInitListener).reInit();
    }

    /**
     * use callback to listen if Branch SDK is initialized successfully or not.
     * Need VPN in China, if not you may get error:
     *  "Trouble initializing Branch.  Request to Branch server timed out. Please check your internet connectivity"
     */
    private Branch.BranchReferralInitListener branchReferralInitListener = new Branch.BranchReferralInitListener() {
        @Override
        public void onInitFinished(JSONObject linkProperties, BranchError error) {
            // do stuff with deep link data (nav to page, display content, etc)
            Log.d("Branch","onInitFinished");
            /*
                Once the Branch SDK initialized successfully,
                lead to OtherActivity.class if linkProperties key=deep_link_test & value contains "other".
             */
            if (error == null) {
                try {
                    String testParam = linkProperties.getString("deep_link_test");
                    if (testParam.contains("other")) {
                        Intent intent = new Intent(MainActivity.this, OtherActivity.class);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.d("Branch","Branch Err:" + error.getMessage());
            }
        }
    };

    /**
     * Create Content Reference
     * @return BranchUniversalObject
     */
    private BranchUniversalObject createContentReference(){
        BranchUniversalObject buo = new BranchUniversalObject()
                .setCanonicalIdentifier("content/12345")
                .setTitle("My Content Title")
                .setContentDescription("My Content Description")
                .setContentImageUrl("https://lorempixel.com/400/400")
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setContentMetadata(new ContentMetadata().addCustomMetadata("deep_link_test", "other"));

        return buo;
    }

    /**
     * Share Branch's Deep Linking
     *
     * ControlParameter in LinkProperties is key-value mapped. Add the params according to a real product.
     * (deep_link_test & other are using for test)
     */
    private void shareDeepLink(){
        LinkProperties lp = new LinkProperties()
                .setChannel("facebook")
                .setFeature("sharing")
                .setCampaign("content 123 launch")
                .setStage("new user")
                .addControlParameter("$desktop_url", "http://example.com/home")
                .addControlParameter("custom", "data")
                .addControlParameter("deep_link_test", "other")
                .addControlParameter("custom_random", Long.toString(Calendar.getInstance().getTimeInMillis()));

        ShareSheetStyle ss = new ShareSheetStyle(MainActivity.this, "Check this out!", "This stuff is awesome: ")
                .setCopyUrlStyle(ContextCompat.getDrawable(this, android.R.drawable.ic_menu_send), "Copy", "Added to clipboard")
                .setMoreOptionStyle(ContextCompat.getDrawable(this, android.R.drawable.ic_menu_search), "Show more")
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.FACEBOOK)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.EMAIL)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.MESSAGE)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.HANGOUT)
                .setAsFullWidthStyle(true)
                .setSharingTitle("Share With");

        createContentReference().showShareSheet(this, lp,  ss,  new Branch.BranchLinkShareListener() {
            @Override
            public void onShareLinkDialogLaunched() {
            }
            @Override
            public void onShareLinkDialogDismissed() {
            }
            @Override
            public void onLinkShareResponse(String sharedLink, String sharedChannel, BranchError error) {
            }
            @Override
            public void onChannelSelected(String channelName) {
            }
        });
    }
}