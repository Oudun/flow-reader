package com.veve.flowreader.uitest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.VectorDrawable;
import android.support.test.rule.ActivityTestRule;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.veve.flowreader.Constants;
import com.veve.flowreader.R;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.views.MainActivity;
import com.veve.flowreader.views.PageActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

public class PageTest extends BookTest {

    PageActivity pageActivity;
    VectorDrawable iconDrawableToPhone;
    VectorDrawable iconDrawableToBook;

    @Rule
    public ActivityTestRule<PageActivity> pageActivityRule =
            new ActivityTestRule<>(
                    PageActivity.class,
                    true,     // initialTouchMode
                    false);   // launchActivity. False to customize the intent

    @Rule
    public ActivityTestRule<MainActivity> mainActivityRule =
            new ActivityTestRule<>(
                    MainActivity.class,
                    true,     // initialTouchMode
                    false);   // launchActivity. False to customize the intent

    @Before
    public void getIcons() {
        iconDrawableToPhone = (VectorDrawable)appContext.getResources().getDrawable(R.drawable.ic_to_phone);
        iconDrawableToBook = (VectorDrawable)appContext.getResources().getDrawable(R.drawable.ic_to_book);
    }

    @Before
    public void getActivity() {
        Intent intent = new Intent("com.veve.flowreader.views.PageActivity");
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.BOOK_ID, testBookId);
        intent.putExtra(Constants.POSITION, 1);
        pageActivity = pageActivityRule.launchActivity(intent);
    }

    @Test
    public void testShowButton() {
        ImageButton showButton = pageActivity.findViewById(R.id.show);
        assertTrue(showButton.getVisibility() == VISIBLE);
        VectorDrawable iconDrawable;
        iconDrawable = (VectorDrawable)(showButton.getDrawable());
        assertTrue(pageActivity.findViewById(R.id.bottomBar).getVisibility() == INVISIBLE);
        assertTrue(areDrawablesIdentical(iconDrawable, iconDrawableToPhone));
        showButton.callOnClick();
        iconDrawable = (VectorDrawable)(showButton.getDrawable());
        assertTrue(areDrawablesIdentical(iconDrawable, iconDrawableToBook));
    }

    @Test
    public void testSwitchShowButton() throws Exception {
        ImageButton showButton = pageActivity.findViewById(R.id.show);
        VectorDrawable iconDrawable;
        iconDrawable = (VectorDrawable)(showButton.getDrawable());
        assertTrue(areDrawablesIdentical(iconDrawable, iconDrawableToPhone));

        //changing mode to "reflowed page"
        showButton.callOnClick();
        iconDrawable = (VectorDrawable)(showButton.getDrawable());
        assertTrue(areDrawablesIdentical(iconDrawable, iconDrawableToBook));

        //getting back to main activity
        Intent mainActivityIntent = new Intent("com.veve.flowreader.views.MainActivity");
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainActivityRule.launchActivity(mainActivityIntent);
        Thread.sleep(3000);

        //getting back to page
//        Intent intent = new Intent("com.veve.flowreader.views.PageActivity");
//        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
//        intent.putExtra(Constants.BOOK_ID, testBookId);
//        intent.putExtra(Constants.POSITION, 1);
        pageActivity = pageActivityRule.getActivity();
//        Thread.sleep(3000);

        //Confirming button is still "book"
        showButton = pageActivity.findViewById(R.id.show);
        iconDrawable = (VectorDrawable)(showButton.getDrawable());
        assertTrue(areDrawablesIdentical(iconDrawable, iconDrawableToPhone));

    }


    @Test
    public void testZoom() {
        assertEquals(pageActivity.getBook().getZoom(), 1.0F);
        ImageButton zoomInButton = pageActivity.findViewById(R.id.larger_text);
        zoomInButton.callOnClick();
        assertEquals(pageActivity.getBook().getZoom(), 1.25F);
        ImageButton zoomOutButton = pageActivity.findViewById(R.id.smaller_text);
        zoomOutButton.callOnClick();
        assertEquals(pageActivity.getBook().getZoom(), 1.0F);
    }

//    @Test
//    public void testPinch() {
//        assertEquals(pageActivity.getBook().getZoom(), 1.0F);
//        LinearLayout page = pageActivity.findViewById(R.id.page);
////        MotionEvent pinchEvent = MotionEvent.obtain(long downTime, long eventTime, int action,
////        float x, float y, int metaState);
//        MotionEvent pinchEvent = MotionEvent.obtain(10 , 10, MotionEvent.ACTION_DOWN,
//        0, 0, );
//        page.onTouchEvent(pinchEvent);
//    }



}
