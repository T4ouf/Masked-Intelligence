package webServer;

//Java standard lib imports
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;

import javax.ws.rs.Consumes;
//JAX-RS (Java REST API implementation)
//Might be needed at some pointimport javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;


//Home classes

@Path("/")
public class RESTAPI {

	private static final String BASE = "../Clients/Web/";
	private static final String RESOURCEFOLDERPATH = "../rsc/";

	public RESTAPI() throws IOException {

	}

	/**
	 * Method with no path that provides the HTML content to the user
	 * @return a FileInputStream with the content of the HTML page
	 * @throws IOException, if failed to access the HTML file
	 */
	@GET
	@Produces(MediaType.TEXT_HTML)
	public InputStream getIndex() throws IOException {
		return new FileInputStream(BASE + "index.html");//"panels/widgetPanel/widgetPanel.html");
	}

	/**
	 *
	 * @return a FileInputStream with the content of the dependency (depends on what the HTML page needs)
	 * @throws IOException, if failed to access the file
	 */
	@Path("/rsc/{fileName: .+}")
	@GET
	public InputStream getRsc(@PathParam("fileName") String fileName) throws IOException {
		System.out.println(RESOURCEFOLDERPATH + fileName);
		return new FileInputStream(RESOURCEFOLDERPATH + fileName);
	}

	/**
	 * Method with "/filePath" path that serves all the common dependencies (CSS + JS content to serve to the user)
	 * @return a FileInputStream with the content of the dependency (depends on what the HTML page needs)
	 * @throws IOException, if failed to access the file
	 */
	@Path("{path: .+}")
	@GET
	@Produces({ "text/html", "text/css", "application/javascript" })
	public Response getFile(@PathParam("path") String path) throws IOException {

		// TODO remove or manage this part
		if (path.contains(".map") || path.contains(".ico")) {
			System.err.println("Not sending debug files NOR favico");
			return Response.noContent().build();
		}

		System.out.println(BASE + path);
		ResponseBuilder r = Response.ok(new FileInputStream(BASE + path));
		if(path.contains(".js")) {
			r.type("application/javascript");
		}

		return r.build();
	}



}
