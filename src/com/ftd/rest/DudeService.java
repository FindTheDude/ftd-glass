package com.ftd.rest;

import java.util.List;

import com.ftd.rest.model.DudeInformation;
import com.ftd.rest.model.DudeInformationWrapper;

import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Path;

public interface DudeService {

	/**
	 * Returns a {@linkplain DudeInformation} list with all the matching
	 * information of people that match the photo sent on the service.  
	 *  
	 * @param userId the dude userId
	 * @param file the user picture
	 * @return list of {@linkplain} dude information
	 */
	@POST(value = "/user/{id}/dude")
	public DudeInformationWrapper findDudeInformation();	
}
