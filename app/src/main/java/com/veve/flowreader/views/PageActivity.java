package com.veve.flowreader.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.veve.flowreader.Constants;
import com.veve.flowreader.R;
import com.veve.flowreader.dao.BookRecord;
import com.veve.flowreader.model.BooksCollection;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageRenderer;
import com.veve.flowreader.model.PageRendererFactory;
import com.veve.flowreader.model.impl.DevicePageContextImpl;
import com.veve.flowreader.model.impl.OpenCVPageLayoutParser;
import com.veve.flowreader.model.impl.SimpleLayoutParser;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.veve.flowreader.Constants.MAX_BITMAP_SIZE;
import static com.veve.flowreader.Constants.VIEW_MODE_ORIGINAL;
import static com.veve.flowreader.Constants.VIEW_MODE_PHONE;

public class PageActivity extends AppCompatActivity {

    Set<AsyncTask> runningTasks;
    TextView pager;
    AppBarLayout bar;
    CoordinatorLayout topLayout;
    PageRenderer pageRenderer;
    BookRecord book;
    SeekBar seekBar;
    ProgressBar progressBar;
    FloatingActionButton home;
    FloatingActionButton show;
    LinearLayout page;
    DevicePageContext context;
    PageActivity pageActivity;
    ScrollView scroll;
    int currentPage;
    int viewMode;
    BooksCollection booksCollection;
    LinearLayout bottomBar;
    boolean barsVisible;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(getClass().getName(), "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.page_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.opencv_parser);
        menuItem.setChecked(true);
        return true;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int position = getIntent().getIntExtra("position", 0);
        booksCollection = BooksCollection.getInstance(getApplicationContext());
        book = booksCollection.getBook(position);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Constants.VIEW_MODE_PARAM, viewMode);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(getClass().getName(), "onCreate");

        super.onCreate(savedInstanceState);

        runningTasks = new HashSet<>();

        setContentView(R.layout.activity_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        long bookId = getIntent().getLongExtra("bookId", 0);
        booksCollection = BooksCollection.getInstance(getApplicationContext());
        book = booksCollection.getBook(bookId);

        pageRenderer = PageRendererFactory.getRenderer(book);
        currentPage = book.getCurrentPage();

        viewMode = savedInstanceState == null
                ? Constants.VIEW_MODE_PHONE
                : savedInstanceState.getInt(Constants.VIEW_MODE_PARAM) == 0
                        ? Constants.VIEW_MODE_PHONE
                        : savedInstanceState.getInt(Constants.VIEW_MODE_PARAM);

        bar = findViewById(R.id.bar);
        topLayout = findViewById(R.id.topLayout);
        pager = findViewById(R.id.pager);
        seekBar = findViewById(R.id.slider);
        home = findViewById(R.id.home);
        progressBar = findViewById(R.id.progress);
        page = findViewById(R.id.page);
        show = findViewById(R.id.show);
        scroll = findViewById(R.id.scroll);
        bottomBar = findViewById(R.id.bottomBar);

        //page.addOnLayoutChangeListener(new LayoutListener());
        seekBar.setMax(book.getPagesCount());
        pager.setOnTouchListener(new PagerTouchListener());
        seekBar.setOnSeekBarChangeListener(new PagerListener());
        home.setOnClickListener(new HomeButtonListener());
        show.setOnClickListener(new ShowListener());
        page.setOnTouchListener(new OnDoubleTapListener(this, page));
        topLayout.addOnLayoutChangeListener(new LayoutListener());

        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        context = new DevicePageContextImpl(point.x);
        context.setZoom(book.getZoom());
        context.setKerning(book.getKerning());
        context.setLeading(book.getLeading());
        context.setMargin(book.getMargin());

        pageActivity = this;
        setPageNumber(currentPage);

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.no_margins: {
                context.setMargin(0);
                book.setMargin(0);
                break;
            }
            case R.id.narrow_margins: {
                context.setMargin(25);
                book.setMargin(25);
                break;
            }
            case R.id.normal_margins: {
                context.setMargin(50);
                book.setMargin(50);
                break;
            }
            case R.id.wide_margins: {
                context.setMargin(100);
                book.setMargin(100);
                break;
            }
            case R.id.opencv_parser: {
                pageRenderer.setPageLayoutParser(
                        OpenCVPageLayoutParser.getInstance());
                setPageNumber(currentPage);
                break;
            }
            case R.id.simple_parser: {
                pageRenderer.setPageLayoutParser(
                        SimpleLayoutParser.getInstance());
                setPageNumber(currentPage);
                break;
            }
            case R.id.page_segmenter: {
                pageRenderer.setPageLayoutParser(
                        SimpleLayoutParser.getInstance());
                setPageNumber(currentPage);
                break;
            }
            case R.id.delete_book: {
                long bookId = book.getId();
                BooksCollection.getInstance(getApplicationContext()).deleteBook(bookId);
                Intent i = new Intent(PageActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                i.putExtra(Constants.BOOK_ID, bookId);
                startActivity(i);
                break;
            }

        }

        setPageNumber(currentPage);
        return true;

    }

    private void setPageNumber(int pageNumber) {
        pager.setText(getString(R.string.ui_page_count, pageNumber + 1, book.getPagesCount()));
        seekBar.setProgress(pageNumber + 1);
        currentPage = pageNumber;
        book.setCurrentPage(pageNumber);
        booksCollection.updateBook(book);
        PageLoader pageLoader = new PageLoader(this);
        kickOthers(pageLoader);
        pageLoader.execute(pageNumber);
    }

    private void kickOthers(PageLoader pageLoader) {
        for (AsyncTask task : runningTasks) {
            if(!task.isCancelled()) {
                task.cancel(true);
                Log.d(getClass().getName(), "Cancelling taks #" + task.hashCode());
            }
        }
        runningTasks.add(pageLoader);
        Log.d(getClass().getName(), "Adding taks #" + pageLoader.hashCode());
    }

////////////////////////////   LISTENERS  ////////////////////////////////////////////////////

        class PagerTouchListener implements View.OnTouchListener {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                view.performClick();
                pager.setVisibility(View.GONE);
                seekBar.setVisibility(VISIBLE);
                return true;
            }
        }

        class OnDoubleTapListener implements View.OnTouchListener {

            private GestureDetector gestureDetector;

            OnDoubleTapListener(Context c, LinearLayout p) {
                gestureDetector = new GestureDetector(c, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        float x = e.getX();

                        if (x > p.getWidth() / 2) {
                            if (book.getCurrentPage() < book.getPagesCount()-1) {
                                setPageNumber(book.getCurrentPage()+1);
                            }
                        } else {
                            if (book.getCurrentPage() > 0) {
                                setPageNumber(book.getCurrentPage()-1);
                            }
                        }
                        return true;
                    }

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        if (barsVisible) {
                            bottomBar.setVisibility(INVISIBLE);
                            bar.setVisibility(INVISIBLE);
                            barsVisible = false;
                        } else {
                            bottomBar.setVisibility(VISIBLE);
                            bar.setVisibility(VISIBLE);
                            barsVisible = true;
                        }
                        return super.onSingleTapConfirmed(e);
                    }

                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }
                });
            }

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.performClick();
                return gestureDetector.onTouchEvent(motionEvent);
            }
        }


        class PagerListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Log.d(getClass().getName(), String.format("onProgressChanged. %d %%", progress));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.d(getClass().getName(), "onStartTrackingTouch");
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.d(getClass().getName(), "onStopTrackingTouch");
            setPageNumber(seekBar.getProgress());
            seekBar.setVisibility(View.GONE);
            pager.setVisibility(VISIBLE);
        }
    }

    class HomeButtonListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            Intent i = new Intent(PageActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);
        }
    }

    class LayoutListener implements View.OnLayoutChangeListener {

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (pageRenderer == null) {
                pageRenderer = PageRendererFactory.getRenderer(book);
            }
            PageMenuListener pageMenuListener = new PageMenuListener();
            findViewById(R.id.smaller_text).setOnClickListener(pageMenuListener);
            findViewById(R.id.larger_text).setOnClickListener(pageMenuListener);
            findViewById(R.id.smaller_kerning).setOnClickListener(pageMenuListener);
            findViewById(R.id.larger_kerning).setOnClickListener(pageMenuListener);
            findViewById(R.id.smaller_leading).setOnClickListener(pageMenuListener);
            findViewById(R.id.larger_leading).setOnClickListener(pageMenuListener);
        }
    }

    class ShowListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            ImageView show = (ImageView)view;
            if (viewMode == VIEW_MODE_ORIGINAL) {
                viewMode = VIEW_MODE_PHONE;
                show.setImageResource(R.drawable.ic_phone);
                Snackbar.make(view, getString(R.string.ui_reflow_page), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Log.d(getClass().getName(), String.format("Setting page #%d for modified page", currentPage));
                findViewById(R.id.smaller_text).setEnabled(true);
                findViewById(R.id.larger_text).setEnabled(true);
                findViewById(R.id.smaller_kerning).setEnabled(true);
                findViewById(R.id.larger_kerning).setEnabled(true);
                findViewById(R.id.smaller_leading).setEnabled(true);
                findViewById(R.id.larger_leading).setEnabled(true);
            } else if (viewMode == VIEW_MODE_PHONE) {
                viewMode = VIEW_MODE_ORIGINAL;
                show.setImageResource(R.drawable.ic_book);
                Snackbar.make(view, getString(R.string.ui_original_page), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Log.d(getClass().getName(), String.format("Setting page #%d for original page", currentPage));
                findViewById(R.id.smaller_text).setEnabled(false);
                findViewById(R.id.larger_text).setEnabled(false);
                findViewById(R.id.smaller_kerning).setEnabled(false);
                findViewById(R.id.larger_kerning).setEnabled(false);
                findViewById(R.id.smaller_leading).setEnabled(false);
                findViewById(R.id.larger_leading).setEnabled(false);
            }
            pageActivity.setPageNumber(currentPage);
        }
    }

    class PageMenuListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.smaller_text: {
                    context.setZoom(0.8f * context.getZoom());
                    Log.v(getClass().getName(), "Zoom set to " + context.getZoom());
                    book.setZoom(context.getZoom());
                    break;
                }
                case R.id.larger_text: {
                    context.setZoom(1.25f * context.getZoom());
                    Log.v(getClass().getName(), "Zoom set to " + context.getZoom());
                    book.setZoom(context.getZoom());
                    break;
                }
                case R.id.smaller_kerning: {
                    context.setKerning(0.8f * context.getKerning());
                    Log.v(getClass().getName(), "Kerning set to " + context.getKerning());
                    book.setKerning(context.getKerning());
                    break;
                }
                case R.id.larger_kerning: {
                    context.setKerning(1.25f * context.getKerning());
                    Log.v(getClass().getName(), "Kerning set to " + context.getKerning());
                    book.setKerning(context.getKerning());
                    break;
                }
                case R.id.smaller_leading: {
                    context.setLeading(0.8f * context.getLeading());
                    Log.v(getClass().getName(), "Leading set to " + context.getLeading());
                    book.setLeading(context.getLeading());
                    break;
                }
                case R.id.larger_leading: {
                    context.setLeading(1.25f * context.getLeading());
                    Log.v(getClass().getName(), "Leading set to " + context.getLeading());
                    book.setLeading(context.getLeading());
                    break;
                }
            }

            pageActivity.setPageNumber(currentPage);

        }


    }

    class PageLoader extends AsyncTask<Integer, Void, Void> {

        private WeakReference<PageActivity> pageActivityReference;
        private PageActivity pageActivity;

        // only retain a weak reference to the activity
        PageLoader(PageActivity context) {
            pageActivityReference = new WeakReference<>(context);
            pageActivity = pageActivityReference.get();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pageActivityReference.get().runningTasks.remove(this);
            Log.d(getClass().getName(), "Task #" + hashCode() + " removed on completion");
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            pageActivity.runningTasks.remove(this);
            Log.d(getClass().getName(), "Task #" + hashCode() + " removed on cancellation");
        }

        @Override
        protected Void doInBackground(Integer... integers) {

            runOnUiThread(()-> {
                pageActivity.scroll.setVisibility(INVISIBLE);
                pageActivity.progressBar.setVisibility(View.VISIBLE);
                }
            );

            int pageNumber = integers[0];

            Bitmap bitmap;

            Log.d(getClass().getName(), String.format("Getting bitmap for zoom = %f", pageActivity.context.getZoom()));

            if (pageActivity.viewMode == Constants.VIEW_MODE_PHONE) {
                bitmap = pageActivity.pageRenderer.renderPage(context, pageNumber);
                Log.v(getClass().getName(), String.format("pageRenderer.renderPage(context, %d)", pageNumber));
            } else {
                bitmap = pageActivity.pageRenderer.renderOriginalPage(pageActivity.context, pageNumber);
                Log.v(getClass().getName(), String.format("pageRenderer.renderOriginalPage(context, %d)", pageNumber));
            }

            Log.d(getClass().getName(), String.format("Result bytes %d", bitmap.getByteCount()));

            int bitmapHeight = bitmap.getHeight();

            runOnUiThread(() -> {
                if (bitmap.getByteCount() > MAX_BITMAP_SIZE) {
                    Snackbar.make(topLayout, getString(R.string.could_not_zoom_more),
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    context.setZoom(context.getZoom()*0.8f);
                    pageActivity.book.setZoom(pageActivity.context.getZoom());
                    pageActivity.booksCollection.updateBook(pageActivity.book);
//                } else if (bitmap.getWidth() >= pageActivity.context.getWidth()) {
                } else {
                    if(viewMode == VIEW_MODE_PHONE) {
                        List<View> pageViews = new ArrayList<>();// UI code goes here
                        for (int offset = 0; offset < bitmapHeight; offset += Constants.IMAGE_VIEW_HEIGHT_LIMIT) {
                            Log.d(getClass().getName(), "Before image creation");
                            int height = Math.min(bitmapHeight, offset + Constants.IMAGE_VIEW_HEIGHT_LIMIT);
                            Log.d(getClass().getName(),
                                    String.format(" Bitmap.createBitmap(bitmap, 0, %d, %d, %d)",
                                            offset, context.getWidth(), height - offset));
                            Log.d(getClass().getName(),
                                    String.format("bitmap size is width : %d height :%d",
                                            bitmap.getWidth(), bitmap.getHeight()));
                            Bitmap limitedBitmap = Bitmap.createBitmap(bitmap, 0, offset, context.getWidth(),
                                    height - offset);
                            ImageView imageView = new ImageView(getApplicationContext());
                            imageView.setScaleType(ImageView.ScaleType.FIT_START);
                            imageView.setImageBitmap(limitedBitmap);
                            pageViews.add(imageView);
                            Log.d(getClass().getName(), "Image creation");
                            Log.d(getClass().getName(), "After image creation");
                        }
                        pageActivity.page.removeAllViews();
                        for (View view : pageViews) {
                            pageActivity.page.addView(view);
                        }
                        Log.v(getClass().getName(), "End setting bitmap");
                    } else {
                        pageActivity.page.removeAllViews();
                        ImageView imageView = new ImageView(getApplicationContext());
                        imageView.setImageBitmap(bitmap);
                        pageActivity.page.addView(imageView);
                    }
                }
                pageActivity.scroll.setVisibility(VISIBLE);
                pageActivity.progressBar.setVisibility(INVISIBLE);
                pageActivity.scroll.scrollTo(0, 0);
            });
            return null;
        }
    }
}
