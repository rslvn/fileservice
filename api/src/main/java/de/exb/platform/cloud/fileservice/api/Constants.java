package de.exb.platform.cloud.fileservice.api;

/**
 * Created by resulav on 23.07.2018.
 */
public class Constants {
	public static final String APPLICATION_FILESERVICE = "fileservice";
	public static final String APPLICATION_FILESERVICE_CLIENT = "fileservice-client";

	public static final String API_FILECONTROLLER = "/api/v1/files";
	public static final String API_CLIENT_FILECONTROLLER = "/api/v1/files/client";

	public static final String API_METHOD_DOWNLOAD = "download";
	public static final String API_METHOD_UPLOAD = "upload";

	protected Constants() {
		// for sonar
	}
}
