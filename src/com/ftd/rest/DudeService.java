package com.ftd.rest;

import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.mime.TypedFile;

import com.ftd.rest.model.DudeInformation;
import com.ftd.rest.model.DudeInformationWrapper;

public interface DudeService {

	/**
	 * Returns a {@linkplain DudeInformation} list with all the matching
	 * information of people that match the photo sent on the service.  
	 *  
	 * @param userId the dude userId
	 * @param file the user picture
	 * @return list of {@linkplain} dude information
	 */
	@Multipart
	@POST(value = "/api/users/{id}/dudes")
	public DudeInformationWrapper findDudeInformation(@Path("id")String userId, @Part("photo") TypedFile photo);	
}
