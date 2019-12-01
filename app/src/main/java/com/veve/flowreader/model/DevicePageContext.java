package com.veve.flowreader.model;

import android.graphics.Canvas;
import android.graphics.Point;

import java.io.Serializable;

/**
 * Created by ddreval on 16.01.2018.
 */

public class DevicePageContext implements Serializable {

    private Point startPoint;
    private Canvas canvas;
    private float zoom = 1;
    private float zoomOriginal = 1;
    private int width;
    private Point remotestPoint;
    private float kerning;
    private float leading;
    private int margin;
    private int lineHeight;
    private int currentBaseLine;
    private boolean newline;

    public float getZoomOriginal() {return zoomOriginal;}

    public void setZoomOriginal(float originalZoom) {this.zoomOriginal = originalZoom;}

    public DevicePageContext() {
        newline = true;
    }

    public DevicePageContext(int width) {
        this.width = width;
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public Point getRemotestPoint() {
        return remotestPoint;
    }

    public float getKerning() {
        return kerning;
    }

    public void setKerning(float kerning) {
        this.kerning = kerning;
    }

    public float getLeading() {
        return leading;
    }

    public void setLeading(float leading) {
        this.leading = leading;
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
        this.margin = margin;
    }

    public int getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    public int getCurrentBaseLine() {
        return currentBaseLine;
    }

    public void setCurrentBaseLine(int currentBaseLine) {
        this.currentBaseLine = currentBaseLine;
    }

    public void resetPosition() {
        startPoint.set(0, 0);
    }

    public boolean isNewline() {
        return newline;
    }

    public void setNewline(boolean newline) {
        this.newline = newline;
    }
}
