package cn.wxreader.worker;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.wxreader.constant.Constant;
import cn.wxreader.domain.User;
import cn.wxreader.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Read {
    private static final Logger log = LoggerFactory.getLogger(Read.class);
    private JSONObject wxReaderData;
    private JSONObject wxReaderHeader;
    private Integer readNum;
    private String wrName;
    private static final Integer SLEEP_INTERVAL = 30;
    private static final String KEY = "3c5c8717f3daf09iop3423zafeqoi";
    private RefreshToken refreshToken = new RefreshToken();
    public Read() {
    }

    public Read(User user) {
        this.wxReaderData = user.getWxReaderData();
        this.wxReaderHeader = user.getWxReaderHeader();
        this.readNum = user.getReadMinute() * 2;
        this.wrName = user.getWrName();
    }

    private Map<String, String> jsonToMap(JSONObject wxReaderHeader) {
        Map<String, String> headerMap = new HashMap<>();
        wxReaderHeader.getInnerMap().forEach((key, value) -> {
            headerMap.put(key, value.toString());
        });
        return headerMap;
    }

    /**
     * Read the book
     * @return Whether the reading was successful
     */
    private Boolean readBook() {
        try {
            wxReaderData.put("ct", Instant.now().getEpochSecond());
            wxReaderData.put("ts", Instant.now().toEpochMilli());
            wxReaderData.put("rn", RandomUtil.randomInt(0, 1000));
            wxReaderData.put("sg", StringUtil.calSha256(wxReaderData.getString("ts") + wxReaderData.getString("rn") + KEY));
            wxReaderData.put("s", StringUtil.calHash(StringUtil.encodeData(wxReaderData.getInnerMap())));

            HttpResponse response = HttpRequest.post(Constant.READ_URL)
                    .headerMap(jsonToMap(wxReaderHeader), true)
                    .body(wxReaderData.toString())
                    .timeout(30000)
                    .execute();

            if (response.body() != null) {
                JSONObject resData = JSON.parseObject(response.body());
                if (resData.isEmpty()) {
                    log.warn("【自动阅读】{}：响应数据为空", wrName);
                    return null;
                }
                if (resData.containsKey("succ")) {
                    if (!resData.containsKey("synckey")) {
                        log.warn("【自动阅读】{}：阅读成功，但未同步数据", wrName);
                        fixNoSyncKey();
                    }
                    return true;
                } else if (-2010 == resData.getInteger("errCode")) {
                    log.error("【自动阅读】{}：用户不存在，请检查读者信息是否正确", wrName);
                    throw new RuntimeException("用户不存在，请检查读者信息是否正确。");
                } else if (-2012 == resData.getInteger("errCode")) {
                    log.info("【自动阅读】{}：Cookie过期，尝试刷新", wrName);
                    return refreshToken.refreshCookie(wxReaderHeader);
                }
                log.warn("【自动阅读】{}：阅读失败，错误码：{}", wrName, resData.getInteger("errCode"));
                return false;
            } else {
                log.error("【自动阅读】{}：响应体为空", wrName);
                throw new RuntimeException("Response body is null");
            }
        } catch (Exception e) {
            log.error("【自动阅读】{}：阅读过程中发生异常", wrName, e);
            throw new RuntimeException("An unexpected exception occurs during reading.", e);
        } finally {
            wxReaderData.remove("s");
        }
    }

    private void fixNoSyncKey() {
        HttpRequest.post(Constant.FIX_SYNCKEY_URL)
                .headerMap(jsonToMap(wxReaderHeader), true)
                .body(Constant.FIX_SYNCKEY_BODY)
                .timeout(30000)
                .execute();
    }

    public String startRead() {
        refreshToken.refreshCookie(wxReaderHeader);
        int failCount = 0;
        int successCount = 0;
        
        for (int i = 0; i < readNum; ) {
            try {
                Boolean readRes = readBook();
                if (readRes == null) {
                    if (failCount < 3) {
                        failCount++;
                        int sleepTime = SLEEP_INTERVAL + RandomUtil.randomInt(0, 10);
                        log.warn("【自动阅读】{}：阅读失败，第{}次重试，等待{}秒", wrName, failCount, sleepTime);
                        ThreadUtil.sleep(sleepTime, TimeUnit.SECONDS);
                    } else {
                        log.error("【自动阅读】{}：连续3次阅读失败，终止阅读", wrName);
                        throw new RuntimeException("连续3次阅读失败，请检查网络或读者信息。");
                    }
                } else if (readRes) {
                    successCount++;
                    int sleepTime = SLEEP_INTERVAL + RandomUtil.randomInt(0, 10);
                    ThreadUtil.sleep(sleepTime, TimeUnit.SECONDS);
                    i++;
                    log.info("【自动阅读】{}：第{}次阅读成功，本次阅读时长{}秒", wrName, i, sleepTime);
                } else {
                    log.error("【自动阅读】{}：阅读失败，终止阅读", wrName);
                    throw new RuntimeException("【自动阅读】阅读失败，请检查读者信息是否正确。");
                }
            } catch (Exception e) {
                log.error("【自动阅读】{}：阅读过程中发生异常，已成功阅读{}次", wrName, successCount, e);
                throw new RuntimeException("阅读过程中发生异常，已成功阅读" + successCount + "次", e);
            }
        }
        
        log.info("【自动阅读】{}：今日阅读已完成，成功阅读{}次，总计阅读{}分钟", wrName, successCount, readNum / 2);
        return String.format("【自动阅读】%s：今日阅读已完成，成功阅读%d次，总计阅读%d分钟", wrName, successCount, readNum / 2);
    }
}