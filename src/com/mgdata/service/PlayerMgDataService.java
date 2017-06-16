package com.mgdata.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springside.modules.utils.collection.CollectionUtil;

import com.jd.open.api.sdk.internal.util.StringUtil;
import com.kg.live.entity.ApiInfoEntity;
import com.kg.live.entity.ApiInfoEntityExample;
import com.kg.live.entity.MgApiUserEntity;
import com.kg.live.entity.PlayerMgData;
import com.kg.live.mapper.ApiInfoEntityMapper;
import com.kg.live.mapper.MgApiUserEntityMapper;
import com.kg.live.mapper.PlayerMgDataMapper;
import com.mgdata.util.HttpsRequestUtil;
import com.mgdata.util.LiveConfig;

@Service
public class PlayerMgDataService {
	private static final Logger logger = LoggerFactory.getLogger(PlayerMgDataService.class);
	@Autowired
	private ApiInfoEntityMapper apiInfoMapper;
	private List<ApiInfoEntity> aipInfoList;

	@Autowired
	private PlayerMgDataMapper playerMgDataMapper;

	@Autowired
	private MgApiUserEntityMapper mgApiUserEntityMapper;

	@PostConstruct
	public void initapiInfoList() {
		ApiInfoEntityExample e = new ApiInfoEntityExample();
		e.createCriteria().andLiveIdEqualTo(LiveConfig.MG_LIVE_ID).andStateEqualTo(LiveConfig.NORMAL_STATE);
		aipInfoList = apiInfoMapper.selectByExample(e);
		logger.info("初始化list完成......");
	}

	/**
	 * getDataFromMgSys 从Mg系统中查询代理 下的玩家下单据情况
	 * 
	 * @throws DocumentException
	 */
	public void getDataFromMgSys() throws DocumentException {
		logger.info("从MG中拉取数据 开始了。。。。。");

		List<ApiInfoEntity> aipInfoList = this.getAipInfoList();
		if (aipInfoList == null || aipInfoList.size() == 0) {
			logger.error("配置 信息出错，请重新 配置 ");
		}
		ApiInfoEntity apiInfo = null;
		for (ApiInfoEntity apiInfoEntity : aipInfoList) {
			if (apiInfoEntity.getLiveName().equalsIgnoreCase("mg")) {
				apiInfo = apiInfoEntity;
				break;
			}
		}
		if (apiInfo == null || apiInfo.getState() != 50) {
			logger.error("配置 信息出错，请重新 配置 ");
		}

		// 取出当前MG系统的所有的玩家，都在其代理 下面
		List<MgApiUserEntity> mgApiUserEntityList = this.queryMgUserByProxy(apiInfo);
		if (CollectionUtil.isEmpty(mgApiUserEntityList)) {
			logger.info("请求到玩家的集合为空 mgApiUserEntityList" + mgApiUserEntityList + " 退出本次的MG中玩家下单的抽取 ");
			return;

		}
		// 查询最后拉取的最大的rowID记录
		PlayerMgData pullDateMg = this.getLatestPullDateMg(null, null);
		Boolean find = true;
		List<PlayerMgData> playerMgDatas = new ArrayList();
		// 第一次拉取数据
		Long rowId = pullDateMg == null ? 0L : pullDateMg.getRowId();
		while (find) {

			JSONObject obj = new JSONObject();
			obj.element("LastRowId", rowId);

			JSONObject object = HttpsRequestUtil.httpsRequestJson(apiInfo.getWebUrl(), apiInfo.getAgent(), apiInfo.getLiveKey(), "POST", "GetSpinBySpinData", obj.toString());
			if (null == object) {
				logger.info("此轮请求的数据为空" + object);
				return;
			}
			logger.info("返回值为==" + object);
			JSONObject jStatus = object.getJSONObject("Status");
			JSONArray jsonArray = object.getJSONArray("Result");// JSONArray.fromObject();
			String errCode = jStatus.getString("ErrorCode");
			if ("0".equalsIgnoreCase(errCode) && !StringUtil.isEmpty(jsonArray.toString())) {

				List<PlayerMgData> tempList = (List<PlayerMgData>) JSONArray.toCollection(jsonArray, PlayerMgData.class);
				if (!CollectionUtil.isEmpty(tempList)) {
					int size = tempList.size();
					pullDateMg = tempList.get(size - 1);
					playerMgDatas.addAll(tempList);
				} else {
					find = false;
					logger.info("此轮请求时返回的result为空值==");
				}

			} else {
				find = false;
				logger.info("此轮请求已完成==" + object);
			}
		}
		// 这里要对接收到的数据进行清洗，将不是本系统的代理下的玩家下单情况不用接收进来
		if (!CollectionUtils.isEmpty(playerMgDatas)) {
			Map<String, Object> map = cleanDate(mgApiUserEntityList, playerMgDatas);
			rowId = (Long) map.get("rowId");
			List<PlayerMgData> pullDateMgs = (List<PlayerMgData>) map.get("list");
			if (!CollectionUtils.isEmpty(pullDateMgs)) {
				this.insertBatchPullDateMgs(pullDateMgs);
			}

		}

	}

	/**
	 * 
	 * @param mgApiUserEntityList
	 *            代理下面的玩家
	 * @param pullDateMgList
	 *            MG下面的玩家下单据的情况
	 * @return 在本系统中的玩家下单据的情况及最大的接收rowId值，以供循环使用的参数
	 */
	public Map<String, Object> cleanDate(List<MgApiUserEntity> mgApiUserEntityList, List<PlayerMgData> pullDateMgList) {
		Map<String, Object> map = new HashMap();
		if (CollectionUtil.isEmpty(mgApiUserEntityList) || CollectionUtil.isEmpty(pullDateMgList)) {
			return map;
		}
		List<PlayerMgData> tempList = new ArrayList();
		Long rowId = 0L;
		for (int i = 0; i < pullDateMgList.size() - 1; i++) {
			PlayerMgData pullDateMg = pullDateMgList.get(i);
			boolean find = findPlayerInUserEntityList(pullDateMg, mgApiUserEntityList);
			if (find) {
				rowId = pullDateMg.getRowId() > rowId ? pullDateMg.getRowId() : rowId;
				tempList.add(pullDateMg);
			}
		}
		map.put("rowId", rowId);

		map.put("list", tempList);
		return map;

	}

	public boolean findPlayerInUserEntityList(PlayerMgData pullDateMg, List<MgApiUserEntity> mgApiUserEntityList) {
		boolean find = false;
		for (int i = 0; i < mgApiUserEntityList.size() - 1; i++) {
			MgApiUserEntity mgApiUserEntity = mgApiUserEntityList.get(i);
			if (mgApiUserEntity.getUsername().equalsIgnoreCase(pullDateMg.getAccountNumber())) {
				find = true;
				break;
			}
		}
		return find;
	}

	public void insertBatchPullDateMgs(List<PlayerMgData> pullDateMgs) {
		int result = playerMgDataMapper.insertPullDateMgBatch(pullDateMgs);
		if (result > 0) {
			logger.info("批量插入数据成功 ");
		} else {
			logger.info("批量插入数据失败 ");
		}

	}

	public PlayerMgData getLatestPullDateMg(String accountNumber, String rowId) {
		logger.info("查询最大的rowid的记录参数accountNumber=  " + accountNumber + " rowId=" + rowId);
		Map<String, Object> map = new HashMap();
		if (!StringUtil.isEmpty(accountNumber)) {
			map.put("accountNumber", accountNumber);
		}
		if (!StringUtil.isEmpty(rowId + "")) {
			map.put("rowId", rowId);
		}
		List<PlayerMgData> pullDateMgList = playerMgDataMapper.selectByMap(map);
		return (pullDateMgList != null && pullDateMgList.size() > 0) ? pullDateMgList.get(0) : null;
	}

	public List<ApiInfoEntity> getAipInfoList() {
		return aipInfoList;
	}

	public void setAipInfoList(List<ApiInfoEntity> aipInfoList) {
		this.aipInfoList = aipInfoList;
	}

	public List<MgApiUserEntity> queryMgUserByProxy(ApiInfoEntity apiInfo) throws DocumentException {

		List<MgApiUserEntity> tempList = mgApiUserEntityMapper.selectByAgentName(apiInfo.getAgent());

		// 1:测试 代理的合法性
		// Map<String, Object> map = new HashMap();
		// List<MgApiUserEntity> tempList = new ArrayList();
		// map.put("param0", apiInfo.getAgent());
		// map.put("param1", apiInfo.getLiveKey());
		// String requestXML =
		// HttpsRequestUtil.soapXMLReplace(LiveConfig.soapIsAuthenticateXMl,
		// map);
		// String resultXML = HttpsRequestUtil.httpRequest(LiveConfig.SOAP_URL,
		// "POST", requestXML, "IsAuthenticate");
		// logger.info("代理登录的XML请求: " + requestXML + " , 请求的地址为: " +
		// LiveConfig.SOAP_URL + ", 请求的结果:" + resultXML);
		// map.clear();
		// if (StringUtils.isEmpty(resultXML)) {
		// return null;
		// }
		// String result = HttpsRequestUtil.getXMLValue(resultXML, "ErrorCode");
		// // result=0,代理登录成功，里面有SessionGUID
		// if (result.equalsIgnoreCase("0")) {
		// // 2 取得有次的玩家
		// String url = apiInfo.getReporturl() + "?proxyName=" +
		// apiInfo.getAgent() + "&password=" + apiInfo.getLiveKey() +
		// "&gameType=mg";
		//
		// JSONArray object = HttpsRequestUtil.httpRequestJson(url,
		// apiInfo.getAgent(), apiInfo.getLiveKey(), "POST", "", null);
		// if (null == object) {
		// logger.info("此轮请求的数据为空" + object);
		// return null;
		// }
		// logger.info("返回值为==" + object);
		// // JSONObject jStatus = object.getJSONObject("SUCCESS");
		// // JSONArray jsonArray = object.getJSONArray("list");//
		// // JSONArray.fromObject();
		// // String errCode = jStatus.getString("SUCCESS");
		// if (!StringUtil.isEmpty(object.toString())) {
		//
		// tempList = (List<MgApiUserEntity>) JSONArray.toCollection(object,
		// MgApiUserEntity.class);
		//
		// } else {
		// return null;
		// }
		// }

		return tempList;
	}

	// 查找某人最近下的一次单的情况，也就是rowId最大值的记录。
	public PlayerMgData getPlayerMgDataByPlayerName(String accountName) {
		logger.info("查询某人最近下单的情况accountName=" + accountName);
		PlayerMgData pullDateMg = this.getLatestPullDateMg(accountName, null);

		return pullDateMg;

	}

	// 查找某人最近下的所有单据的情况
	public List<PlayerMgData> getAllPullDateMgByPlayerName(String accountName) {
		logger.info("查询某人最近下单的情况accountName=" + accountName);
		Map<String, Object> map = new HashMap();
		if (!StringUtil.isEmpty(accountName)) {
			map.put("accountNumber", accountName);
		}
		List<PlayerMgData> pullDateMgs = playerMgDataMapper.selectByMap(map);

		return pullDateMgs;

	}

}
