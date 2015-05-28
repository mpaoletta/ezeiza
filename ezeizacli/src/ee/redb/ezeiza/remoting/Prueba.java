package ee.redb.ezeiza.remoting;

import retrofit.Callback;
import retrofit.ResponseCallback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class Prueba {

	public static void main(String args[]) {
		
		RestAdapter restAdapter = 
				new RestAdapter.Builder()
	    		.setEndpoint("http://192.168.1.102:8080")
	    		.build();
		
		final EzeizaService service = restAdapter.create(EzeizaService.class);			
		
		System.out.println(service.now());
		
		service.configuration(2, 1, new Callback<BoardConfiguration>() {
			
			@Override
			public void success(BoardConfiguration board, Response arg1) {
				System.out.println(board);
				for(CellPhase cell : board.getPhases()) {
					System.out.println(cell.getImageName());
					service.loadImage(cell.getImageName(), new ResponseCallback() {
						@Override
						public void failure(RetrofitError arg0) {
							arg0.printStackTrace();
							System.out.println("Error cargando imagen: " + arg0.getMessage());
						}
						@Override
						public void success(Response arg0) {
							System.out.println(arg0);
						}
					});
				}
			}
			
			@Override
			public void failure(RetrofitError e) {
				e.printStackTrace();
			}
		});
		
		
		try {
			Thread.sleep(10000000l);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
