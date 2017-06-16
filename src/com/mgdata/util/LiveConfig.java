package com.mgdata.util;

public class LiveConfig {

	// 状态定义 -50：删除 0： 未启用 50：正常
	public final static Short DELETE_STATE = -50;
	public final static Short NOT_ENABLE_STATE = 0;// 未启用
	public final static Short NORMAL_STATE = 50;

	/*******************************************/
	// 2:AG视讯厅 11:BBIN视讯厅 15:SA 沙龙
	public static final Integer MG_LIVE_ID = 13;

	public static final String soapIsAuthenticateXMl = "<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\"> <soap12:Body> <IsAuthenticate xmlns=\"https://entservices.totalegame.net\"> <loginName>param0</loginName> <pinCode>param1</pinCode></IsAuthenticate></soap12:Body></soap12:Envelope>";

	public static final String SOAP_URL = "https://entservices.totalegame.net/EntServices.asmx";// mg的soap访问地址

	public final static Byte SA_LIVE = 41;// 视讯
	public final static Byte SA_LOTTO = 52;// lotto
	public final static Byte SA_DIANZHI = 53;// 电子

	/*********************************************/
	// 每页拉取数据数
	public final static Integer BBIN_PAGE_LIMIT = 500;
	// BBin视讯类型
	public final static Integer BBIN_GAME_KIND_LIVE = 3;
	// sa用户名前缀长度
	public final static int SA_NAME_PRE_LENGTH = 3;

	// 视讯输赢定义
	public final static Byte LIVE_RESULT_TYPE_LOSE = 1;// 输
	public final static Byte LIVE_RESULT_TYPE_WIN = 2;// 赢
	public final static Byte LIVE_RESULT_TYPE_HE = 3;// 和
	public final static Byte LIVE_RESULT_TYPE_CANCEL = 4;// 注单取消

	public final static String SECRET_KEY = "BF4CE30CD17A4BF8B4F728C37302E1E5";// Secret
																				// Key
	public final static String MD5KEY = "GgaIMaiNNtg"; // MD5Key MD5鍵

}
