package com.mgdata.controller;

import org.springframework.stereotype.Controller;

/*
 * sa 手动拉取数据
 */
@Controller
public class LiveController {
	// @Autowired
	// private ManLiveService liveService;

	// @RequestMapping(value = "SaGetOneRecord")
	// public @ResponseBody
	// Object manGetOneRecord(HttpServletRequest request) {
	// String date = request.getParameter("date");// 日期
	// String siteId = request.getParameter("siteId");// 网站id
	// if (StringUtils.isBlank(date)) {
	// return "date不能为空";
	// }
	// if (StringUtils.isBlank(siteId)) {
	// return "siteId不能为空";
	// }
	// List<ApiInfoEntity> aipInfoList = liveService.getAipInfoList();
	// if (aipInfoList == null || aipInfoList.size() == 0) {
	// return "site is null";
	// }
	// ApiInfoEntity apiInfo = null;
	// for (ApiInfoEntity apiInfoEntity : aipInfoList) {
	// if (apiInfoEntity.getSiteId() == Integer.valueOf(siteId)) {
	// apiInfo = apiInfoEntity;
	// break;
	// }
	// }
	// if (apiInfo == null || apiInfo.getState() != 50) {
	// return "site is not found";
	// }
	//
	// long start = System.currentTimeMillis();
	// liveService.manGetRecord(apiInfo);
	// long end = System.currentTimeMillis();
	// return "ok,耗时：" + (end - start);
	// }
	//
	// @RequestMapping(value = "SaGetRecord")
	// public @ResponseBody
	// Object manGetRecord(HttpServletRequest request) {
	// long start = System.currentTimeMillis();
	// liveService.manGetRecord();
	// long end = System.currentTimeMillis();
	// return "ok,耗时：" + (end - start);
	// }
}
