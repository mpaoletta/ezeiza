package ee.redb.ezeiza.remoting;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Streaming;

public interface EzeizaService {

	@GET("/configuration/{y}/{x}")
	BoardConfiguration configuration(@Path("y") int y, @Path("x") int x);	
	
	
	@GET("/configuration/{y}/{x}")
	void configuration(@Path("y") int y, @Path("x") int x, Callback<BoardConfiguration> callback);	

    @GET("/image/{imageName}")
    @Streaming
    Response loadImage(@Path("imageName") String filename);	
	
	
    @GET("/image/{imageName}")
    @Streaming
    void loadImage(@Path("imageName") String filename, Callback<Response> callback);	
    
    @GET("/now")
    ServerTimestamp now();
    
}
