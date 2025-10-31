package com.jfeat.module.frontPage.api.app;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.module.frontPage.services.gen.persistence.dao.FrontPageMapper;
import com.jfeat.module.frontPage.services.gen.persistence.model.FrontPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Api("FrontPage RPC")
@RequestMapping("/api/rpc/frontPage")
public class AppFrontPageRpcEndpoint {

    @Resource
    FrontPageMapper frontPageMapper;

    @Value("${rpc.front-page.clientId:}")
    private String rpcClientId;

    @Value("${rpc.front-page.clientSecret:}")
    private String rpcClientSecret;

    private void checkRpcAuth(String clientId, String clientSecret) {
        if (rpcClientId == null || rpcClientId.isEmpty() || rpcClientSecret == null || rpcClientSecret.isEmpty()) {
            throw new BusinessException(BusinessCode.BadRequest.getCode(), "RPC签权未配置");
        }
        if (!rpcClientId.equals(clientId) || !rpcClientSecret.equals(clientSecret)) {
            throw new BusinessException(BusinessCode.BadRequest.getCode(), "签权失败");
        }
    }

    @GetMapping("/getPageContent/{pageId}")
    @ApiOperation(value = "获取页面内容-通过页面ID，仅返回内容JSON")
    public Object getPageContentByPageId(@PathVariable String pageId,
                                         @RequestParam(name = "clientId", required = false) String clientId,
                                         @RequestParam(name = "clientSecret", required = false) String clientSecret) {
        checkRpcAuth(clientId, clientSecret);
        List<FrontPage> list = frontPageMapper.selectList(new QueryWrapper<FrontPage>().eq("page_id", pageId));
        if (list == null || list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            throw new BusinessException(BusinessCode.BadRequest.getCode(), "指定页面ID查询到多个页面，请保证唯一性");
        }
        FrontPage page = list.get(0);
        String content = page.getContent();
        return content == null || content.isEmpty() ? null : JSONObject.parse(content);
    }

    @GetMapping("/getPageContent/by/{channelNo}")
    @ApiOperation(value = "获取页面内容-通过渠道编号，仅返回内容JSON")
    public Object getPageContentByChannelNo(@PathVariable String channelNo,
                                            @RequestParam(name = "clientId", required = false) String clientId,
                                            @RequestParam(name = "clientSecret", required = false) String clientSecret) {
        checkRpcAuth(clientId, clientSecret);
        List<FrontPage> list = frontPageMapper.selectList(new QueryWrapper<FrontPage>().eq("channel_no", channelNo));
        if (list == null || list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            throw new BusinessException(BusinessCode.BadRequest.getCode(), "指定渠道编号查询到多个页面，请保证唯一性");
        }
        FrontPage page = list.get(0);
        String content = page.getContent();
        return content == null || content.isEmpty() ? null : JSONObject.parse(content);
    }
}


