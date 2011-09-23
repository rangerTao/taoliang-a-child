package com.duole.layout;

import com.duole.R;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MusicGallery extends Gallery {

	private Camera mCamera = new Camera();
	private int mMaxRotationAngle = 60;
	private int mMaxZoom = -300;
	private int mCoveflowCenter;

	public MusicGallery(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MusicGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setStaticTransformationsEnabled(true);
	}

	public MusicGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setStaticTransformationsEnabled(true);
	}

	private static int getCenterOfView(View view) {
		return view.getLeft() + view.getWidth() / 2;
	}

	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {

		t.setTransformationType(Transformation.TYPE_MATRIX);
		int childCenter = getCenterOfView(child);

		if (childCenter == mCoveflowCenter) {
			transformImageBitmap((LinearLayout) child, t, 0);
		}

		return super.getChildStaticTransformation(child, t);
	}

	private int getCenterOfMusicGallery() {
		return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2
				+ getPaddingLeft();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mCoveflowCenter = getCenterOfMusicGallery();
		super.onSizeChanged(w, h, oldw, oldh);
	}

	private void transformImageBitmap(LinearLayout child, Transformation t,
			int rotationAngle) {
		
		mCamera.save();
		final Matrix imageMatrix = t.getMatrix();
		final int imageHeight = child.getLayoutParams().height;
		final int imageWidth = child.getLayoutParams().width;

		final int rotation = Math.abs(rotationAngle);

		mCamera.translate(0.0f, 0.0f, 500.0f);

		
		mCamera.rotateY(rotationAngle);
		mCamera.getMatrix(imageMatrix);
		imageMatrix.preTranslate(-(imageWidth / 2), -(imageHeight / 2));
		imageMatrix.postTranslate((imageWidth / 2), (imageHeight / 2));
		mCamera.restore();
	}

}
