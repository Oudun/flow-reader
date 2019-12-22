package com.veve.flowreader.model.impl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import com.veve.flowreader.model.BookSource;
import com.veve.flowreader.model.DevicePageContext;
import com.veve.flowreader.model.PageGlyph;
import com.veve.flowreader.model.PageLayoutParser;
import com.veve.flowreader.model.PageRenderer;

import java.util.Arrays;
import java.util.List;

import static android.graphics.Bitmap.Config.ARGB_8888;

public class PageRendererImpl implements PageRenderer {

    private PageLayoutParser pageLayoutParser;

    private BookSource bookSource;

    public PageRendererImpl(BookSource bookSource) {
        pageLayoutParser = OpenCVPageLayoutParser.getInstance();
        this.bookSource = bookSource;
    }

    @Override
    public List<Bitmap> renderPage(DevicePageContext context, int position) {
        Log.d(getClass().getName(), "1");

        Log.i(getClass().getName(), String.format("position=%d", position));
        List<PageGlyph> pageGlyphList = pageLayoutParser.getGlyphs(bookSource, position);

        if (pageGlyphList.size() <= 1) {
            return Arrays.asList(renderOriginalPage(context, position));
        } else {
            Log.d(getClass().getName(),"2");

            for(PageGlyph pageGlyph : pageGlyphList) {
                pageGlyph.draw(context, false);
            }

            Log.d(getClass().getName(), "3");

            context.setCurrentBaseLine(0);
            Point remotestPoint = context.getRemotestPoint();
            Log.i(getClass().getName(), String.format("w=%d h=%d, position=%d", context.getWidth(), remotestPoint.y, position));
            Bitmap bitmap = Bitmap.createBitmap(context.getWidth(), remotestPoint.y + (int)context.getLeading() , ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            context.resetPosition();
            context.setCanvas(canvas);

            Paint paint1 = new Paint();
            paint1.setStyle(Paint.Style.STROKE);
            paint1.setColor(Color.BLUE);
            paint1.setStrokeWidth(25);

            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint1);

            for(PageGlyph pageGlyph : pageGlyphList) {
                pageGlyph.draw(context, true);
            }

            context.resetPosition();
            context.setCanvas(canvas);
            return Arrays.asList(bitmap);
        }

    }

    @Override
    public Bitmap renderOriginalPage(int position) {
        return bookSource.getPageBytes(position);
    }

    @Override
    public Bitmap renderOriginalPage(DevicePageContext context, int position) {
        Bitmap bitmap = bookSource.getPageBytes(position);
        return Bitmap.createScaledBitmap(bitmap,
                (int) (context.getZoomOriginal() * bitmap.getWidth()),
                (int) (context.getZoomOriginal() * bitmap.getHeight()),
                false);
    }

    public void setPageLayoutParser(PageLayoutParser pageLayoutParser) {
        this.pageLayoutParser = pageLayoutParser;
    }

}
