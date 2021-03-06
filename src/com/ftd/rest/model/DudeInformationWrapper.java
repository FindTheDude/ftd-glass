package com.ftd.rest.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class DudeInformationWrapper {

	@SerializedName(value = "tags")
	List<DudeInformation> dudeInformation = new ArrayList<DudeInformation>();

	public List<DudeInformation> getDudeInformation() {
		return dudeInformation;
	}

	public void setDudeInformation(List<DudeInformation> dudeInformation) {
		this.dudeInformation = dudeInformation;
	}	
}
