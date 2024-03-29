/*
 * Copyright (c) 2010, Sony Ericsson Mobile Communication AB. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 *    * Redistributions of source code must retain the above copyright notice, this 
 *      list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *    * Neither the name of the Sony Ericsson Mobile Communication AB nor the names
 *      of its contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.ece194.pan;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * View capable of drawing an image at different zoom state levels
 */
public class ImageViewer extends View implements Observer {

    /** Paint object used when drawing bitmap. */
    private final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

    /** Rectangle used (and re-used) for cropping source image. */
    private final Rect mRectSrc = new Rect();

    /** Rectangle used (and re-used) for specifying drawing area on canvas. */
    private final Rect mRectDst = new Rect();
    
    /** Rectangle used for the out-of-bounds source */
    private final Rect mRectOOBSrc = new Rect();
    
    /** Rectangle used for the out-of-bounds destination */
    private final Rect mRectOOBDst = new Rect();

    /** The bitmap that we're zooming in, and drawing on the screen. */
    private Bitmap mBitmap;

    /** State of the zoom. */
    private PanState mState;

    // Public methods

    /**
     * Constructor
     */
    public ImageViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Set image bitmap
     * 
     * @param bitmap The bitmap to view and zoom into
     */
    public void setImage(Bitmap bitmap) {
        mBitmap = bitmap;

        invalidate();
    }

    /**
     * Set object holding the zoom state that should be used
     * 
     * @param state The zoom state
     */
    public void setPanState(PanState state) {
        if (mState != null) {
            mState.deleteObserver(this);
        }

        mState = state;
        mState.addObserver(this);

        invalidate();
    }

    // Superclass overrides

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null && mState != null) {
            final int viewWidth = getWidth();
            final int viewHeight = getHeight();
            final int bitmapWidth = mBitmap.getWidth();
            final int bitmapHeight = mBitmap.getHeight();

            final float panX = mState.getPanX();
            //final float panY = mState.getPanY();
            final float scaleFactor = (float)bitmapHeight / (float)viewHeight;
            
            // Setup source and destination rectangles
            mRectSrc.left = (int)(panX * bitmapWidth - viewWidth * scaleFactor/ 2);
            mRectSrc.top = 0;
            mRectSrc.right = (int)(mRectSrc.left + viewWidth * scaleFactor);
            mRectSrc.bottom = bitmapHeight;
            mRectDst.left = getLeft();
            mRectDst.top = getTop();
            mRectDst.right = getRight();
            mRectDst.bottom = getBottom();

            // If the source is out of bounds of the image, use the OOB rects to display the other side of the image
            if (mRectSrc.left < 0) {
            	int delta = -mRectSrc.left;
                mRectSrc.left = 0;
                mRectDst.left += (int)((float)delta/scaleFactor);
                mRectOOBSrc.left = bitmapWidth-delta;
                mRectOOBSrc.top = 0;
                mRectOOBSrc.bottom = bitmapHeight;
                mRectOOBSrc.right = bitmapWidth;
                mRectOOBDst.left = getLeft();
                mRectOOBDst.top = getTop();
                mRectOOBDst.right = (int)((float)delta/scaleFactor);
                mRectOOBDst.bottom = getBottom();
                canvas.drawBitmap(mBitmap, mRectOOBSrc, mRectOOBDst, mPaint);
            }
            if (mRectSrc.right > bitmapWidth) {
            	int delta = mRectSrc.right - bitmapWidth;
                mRectSrc.right = bitmapWidth;
                mRectDst.right -= (int)((float)delta/scaleFactor);
                mRectOOBSrc.left = 0;
                mRectOOBSrc.top = 0;
                mRectOOBSrc.bottom = bitmapHeight;
                mRectOOBSrc.right = delta;
                mRectOOBDst.left = getRight() - (int)((float)delta/scaleFactor);
                mRectOOBDst.top = getTop();
                mRectOOBDst.right = getRight();
                mRectOOBDst.bottom = getBottom();
                canvas.drawBitmap(mBitmap, mRectOOBSrc, mRectOOBDst, mPaint);
            }

            canvas.drawBitmap(mBitmap, mRectSrc, mRectDst, mPaint);
        }
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        invalidate();
    }

    // implements Observer
    public void update(Observable observable, Object data) {
        invalidate();
    }

}
