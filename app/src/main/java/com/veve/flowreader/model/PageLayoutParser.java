package com.veve.flowreader.model;


import android.graphics.Bitmap;

import java.util.List;

/**
 * Component designed to recognize page layout (columns, lines, images) and parse it into glyphs
 * of proper type
 */
public interface PageLayoutParser {

    List<PageGlyph> getGlyphs(BookSource bookSource, int position);

}
