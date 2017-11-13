package org.continuity.wessbas.entities;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import m4jdsl.WorkloadModel;

/**
 * @author Henning Schulz
 *
 */
public class WorkloadModelStorageEntry {

	@JsonIgnore
	private WorkloadModel workloadModel;

	@JsonProperty("created-date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
	private Date createdDate;

	private String id;

	/**
	 * Gets {@link #workloadModel}.
	 *
	 * @return {@link #workloadModel}
	 */
	public WorkloadModel getWorkloadModel() {
		return this.workloadModel;
	}

	/**
	 * Sets {@link #workloadModel}.
	 *
	 * @param workloadModel
	 *            New value for {@link #workloadModel}
	 */
	public void setWorkloadModel(WorkloadModel workloadModel) {
		this.workloadModel = workloadModel;
	}

	/**
	 * Gets {@link #createdDate}.
	 *
	 * @return {@link #createdDate}
	 */
	public Date getCreatedDate() {
		return this.createdDate;
	}

	/**
	 * Sets {@link #createdDate}.
	 *
	 * @param createdDate
	 *            New value for {@link #createdDate}
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * Gets {@link #id}.
	 *
	 * @return {@link #id}
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Sets {@link #id}.
	 *
	 * @param id
	 *            New value for {@link #id}
	 */
	public void setId(String id) {
		this.id = id;
	}

}
