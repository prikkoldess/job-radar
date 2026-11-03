package com.jobscan;

import org.springframework.boot.SpringApplication;

import com.jobscan.JobscanApplication;

public class TestJobscanApplication {

	public static void main(String[] args) {
		SpringApplication.from(JobscanApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
