package com.patho.main.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.patho.main.config.util.ResourceBundle;
import com.patho.main.repository.service.impl.AbstractRepositoryCustom;

public class AbstractService extends AbstractRepositoryCustom {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	protected ResourceBundle resourceBundle;
}