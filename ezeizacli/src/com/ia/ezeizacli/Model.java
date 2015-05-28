package com.ia.ezeizacli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.ResponseCallback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import ee.redb.ezeiza.remoting.BoardConfiguration;
import ee.redb.ezeiza.remoting.CellPhase;
import ee.redb.ezeiza.remoting.EzeizaService;

public class Model {

	private int x = 1;
	private int y = 2;
	private String ip = "localhost";
	
	private BoardConfiguration boardConfig = null;
	
	private Map<String, Bitmap> bitmaps = Collections.synchronizedMap(new HashMap<String, Bitmap>());
	
	private Handler handler;
	
	public Model(Handler _handler) {
		handler = _handler;
	}
	
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public void loadConf() {
		ServiceAdapter serviceAdapter = new ServiceAdapter();
		serviceAdapter.loadConf();
	}
	
	public List<Bitmap> getBitmapList() {
		List<Bitmap> images = new ArrayList<Bitmap>();
		for(CellPhase cell : boardConfig.getPhases()) {
			images.add(bitmaps.get(cell.getImageName()));
		}
		return images;
	}
	
	public Barrel getBarrel(Barrel.BarrelAnimation listener) {
		return new Barrel(bitmaps, boardConfig.getOrdering(), listener);
	}
	
	
	class ServiceAdapter {
		
		RestAdapter restAdapter = 
				new RestAdapter.Builder()
	    		.setEndpoint("http://" + ip + ":8080")
	    		.build();
		
		final EzeizaService service = restAdapter.create(EzeizaService.class);		
		
		public void loadConf() {
			
			boardConfig = service.configuration(getY(), getX());
			System.out.println("Configuracion cargada: " + boardConfig.getConfigId());
			System.out.println("Inicio: " + boardConfig.getStartTime());
			System.out.println("Orden en tambor: ");
			for(String img : boardConfig.getOrdering()) {
				System.out.println(img);
			}
			System.out.println("Fases: ");
			for(CellPhase fase : boardConfig.getPhases()) {
				System.out.println(fase.getImageName());
			}
			
			System.out.println(boardConfig);
			for(final String imageName : boardConfig.getOrdering()) {
				System.out.println("Cargando imagen " + imageName);
				try {
					Response response = service.loadImage(imageName);
					Bitmap bitmap = BitmapFactory.decodeStream(response.getBody().in());
					bitmaps.put(imageName, bitmap);
					System.out.println("Imagen cargada: " + imageName);
				} catch (Exception e) {
					System.out.println("Error en carga o decodificacion de bitmap: " + imageName);
					e.printStackTrace();
				}
			}		
			
/*			
			service.configuration(getY(), getX(), new Callback<BoardConfiguration>() {
				
				@Override
				public void success(BoardConfiguration board, Response arg1) {
					
					boardConfig = board;

					System.out.println(board);
					
					for(final CellPhase cell : board.getPhases()) {
						
						System.out.println("Cargando imagen " + cell.getImageName());
						service.loadImage(cell.getImageName(), new ResponseCallback() {
							@Override
							public void failure(RetrofitError arg0) {
								System.out.println("Error cargando imagen: " + arg0.getMessage());
							}
							@Override
							public void success(Response response) {
								try {
									Bitmap bitmap = BitmapFactory.decodeStream(response.getBody().in());
									bitmaps.put(cell.getImageName(), bitmap);
									System.out.println("Imagen cargada: " + cell.getImageName());
								} catch (IOException e) {
									System.out.println("Error en decodificacion de bitmap: " + cell.getImageName());
									e.printStackTrace();
								}
							}
						});
					}					
					
				}
				
				@Override
				public void failure(RetrofitError e) {
					e.printStackTrace();
				}
			});	
			*/		
		}
		
	}
	
	
	public void startAnimation()  {
		
		
		Runnable animation = new Runnable() {
			
			long id = 0;
			
			@Override
			public void run() {
				while(true) {
					System.out.println("sendMessage " + id);
					Message msg = new Message();
					Bundle data = new Bundle();
					data.putLong("id", id);
					data.putLong("timestamp", System.currentTimeMillis());
					msg.setData(data);
					handler.sendMessage(msg);
					id++;
					try {
						Thread.sleep(2000l);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		
		
		new Thread(animation).start();
		
	}
	
}
