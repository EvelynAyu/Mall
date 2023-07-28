package com.aining.mall.thirdparty;


import com.aliyun.oss.OSS;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MallThirdPartyApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Autowired
    OSS ossClient;

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
