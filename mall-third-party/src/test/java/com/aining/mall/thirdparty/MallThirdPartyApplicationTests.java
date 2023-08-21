package com.aining.mall.thirdparty;


import com.aining.mall.thirdparty.component.SmsComponent;
import com.aining.mall.thirdparty.util.HttpUtils;
import com.aliyun.oss.OSS;
import org.apache.http.HttpResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MallThirdPartyApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Autowired
    OSS ossClient;

    @Resource
    SmsComponent smsComponent;
    @Test
    public void sendSms2(){
        smsComponent.sendSmsCode("13631003251","1234");
    }
    @Test
    public void sendSms(){
        String host = "https://dfsns.market.alicloudapi.com";
        String path = "/data/send_sms";
        String method = "POST";
        String appcode = "0166c67768074e639210e209a37a3eed";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> querys = new HashMap<String, String>();
        Map<String, String> bodys = new HashMap<String, String>();
        bodys.put("content", "code:1234");
        bodys.put("template_id", "CST_ptdie100");
        bodys.put("phone_number", "13631003251");


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUpload() throws FileNotFoundException {
//        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
//        String endpoint = "oss-cn-shenzhen.aliyuncs.com";
//        // 强烈建议不要把访问凭证保存到工程代码里，否则可能导致访问凭证泄露，威胁您账号下所有资源的安全。本代码示例以从环境变量中获取访问凭证为例。运行本代码示例之前，请先配置环境变量。
//        // 填写Bucket名称，例如examplebucket。
//        String accessKeyId = "LTAI5tAACcFVz6ymjh7wiAoC";
//        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
//        String accessKeySecret = "3KS2Plea2yuCl9MsgkDipt2zTnM0i0";

        // 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        // 上传文件流
        InputStream inputStream = new FileInputStream("/Users/laiaining/Documents/JavaWeb/ProjectsDoc/谷粒商城文档/Guli Mall（包含代码、课件、sql）/课件和文档(老版)/基础篇/资料/pics/2b1837c6c50add30.jpg");
        ossClient.putObject("mall-lan", "手机照片3", inputStream);

        // 关闭OSSClient
        ossClient.shutdown();
        System.out.println("success");
    }

}
