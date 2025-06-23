package cn.wxreader.constant;


public class Constant {
    public static String DEFAULT_USER_JSON_PATH = "src/main/resources/user.json";
    public static String UserAgentForIOS = "WeRead/7.4.2 (iPhone; iOS 17.1; Scale/3.00)";
    public static String UserAgentForAndroid = "WeRead/9.0.0 WRBrand/realme Dalvik/2.1.0 (Linux; U; Android 15; RMX3888 Build/UKQ1.231108.001)";
    public static String READ_URL = "https://weread.qq.com/web/book/read";
    public static String RENEW_URL = "https://weread.qq.com/web/login/renewal";
    public static String EXCHANGE_URL = "https://i.weread.qq.com/weekly/exchange";
    public static String FRIEND_RANKING_URL = "https://i.weread.qq.com/friend/ranking?synckey=%s&mine=0";
    public static String FRIEND_LIKE_URL = "https://i.weread.qq.com/friend/like";
    public static String READ_DATA_DETAIL_URL = "https://i.weread.qq.com/readdata/detail?mode=weekly&baseTime=0&defaultPreferBook=0";
    public static String IOS_PLATFORM = "weread_wx-2001-iap-2001-iphone";
    public static String Android_PLATFORM = "wechat_wx-2001-android-100-weread";
    public static String REFRESH_BODY = "{\"rq\":\"%2Fweb%2Fbook%2Fread\"}";
    public static String WX_READ_USERS = "WX_READ_USERS";
    public static String FIX_SYNCKEY_URL = "https://weread.qq.com/web/book/chapterInfos";

    public static String FIX_SYNCKEY_BODY = "{\"bookIds\":[\"22910729\"]}";
}
