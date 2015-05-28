package com.ia.ezeizacli;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;

public class Barrel {

	private Map<String, Bitmap> bitmaps;
	private List<String> ordering;
	private int currentIndex = 0;
	private BarrelAnimation animation;
	
	public Barrel(Map<String, Bitmap> _bitmaps, List<String> _ordering, BarrelAnimation _animation) {
		bitmaps = _bitmaps;
		ordering = _ordering;
		animation = _animation;
	}
	
	private List<Bitmap> topBitmaps;
	private List<Bitmap> bottomBitmaps;
	
	public List<Bitmap> getTopBitmapList() {
		if(topBitmaps == null) {
			topBitmaps = new ArrayList<Bitmap>();
			for(String imageName : ordering) {
				Bitmap original = bitmaps.get(imageName);
				if(original == null) {
					String msg = "Bitmap no encontrado: " + imageName;
					System.out.println(msg);
					throw new RuntimeException(msg);
				}
					
				topBitmaps.add(Bitmap.createBitmap(original, 0, 0, original.getWidth(), (original.getHeight()/2)-1));
			}			 
		}
		return topBitmaps;
	}
	
	public List<Bitmap> getBottomBitmapList() {
		if(bottomBitmaps == null) {
			bottomBitmaps = new ArrayList<Bitmap>();
			for(String imageName : ordering) {
				Bitmap original = bitmaps.get(imageName);
				int height = original.getHeight();
				int half = original.getHeight() / 2;
				System.out.println(height);
				System.out.println(half);
				//bottomBitmaps.add(Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight()/2));
				bottomBitmaps.add(Bitmap.createBitmap(original, 0, half, original.getWidth(), half));
			}			 
		}
		return bottomBitmaps;
	}	
	
	
	interface BarrelAnimation {
		void showNext();
	}
	
	public void show(String imageName) {
		while(!ordering.get(currentIndex).equals(imageName)) {
			currentIndex ++;
			if(currentIndex == ordering.size()) currentIndex = 0;
			animation.showNext();
			rotationPause();
		}
	}
	
	private void rotationPause() {
		
	}
	
	
}
