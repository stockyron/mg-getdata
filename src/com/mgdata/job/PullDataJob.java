package com.mgdata.job;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.mgdata.service.PlayerMgDataService;

public class PullDataJob {
	final static Logger logger = Logger.getLogger(PullDataJob.class);
	@Autowired
	private PlayerMgDataService playerMgDataService;

	public void execute() {
		logger.info("MG 拉取数据进入定时任务检查....");
		try {
			playerMgDataService.getDataFromMgSys();
		} catch (Exception e) {
			logger.error(" MG getDataFromMgSys error!", e);
			e.printStackTrace();
		}
	}
}
