package com.jfeat.fs.api;

import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.tips.ErrorTip;
import com.jfeat.crud.base.tips.SuccessTip;
import com.jfeat.crud.base.tips.Tip;
import com.jfeat.fs.dto.resp.UploadResp;
import com.jfeat.fs.service.LoadFileCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Api(value = "device-fs-upload")
@RestController
public class DeviceFileServiceEndpoint {
    protected final static Logger logger = LoggerFactory.getLogger(DeviceFileServiceEndpoint.class);

    @Autowired
    LoadFileCodeService loadFileCodeService;

    @ApiOperation(value = "设备通用表单上传文件", response = UploadResp.class)
    @PostMapping("/api/d/fs/uploadByForm")
    public Tip uploadByForm(@RequestParam MultipartFile file,
                            @RequestParam @ApiParam(value = "文件路径 /images/head/", required = true) String filePath,
                            @RequestParam(required = false) @ApiParam("文件名（例如：aa.jpg 没后缀服务端使用文件后缀）可选 为空使用uuid") String fileName,
                            @RequestParam(required = false) @ApiParam("功能模块 方便定位问题") String module,
                            @RequestParam(required = false) @ApiParam("是否使用原文件名") Boolean useOriName) {
        try {
            return SuccessTip.create(loadFileCodeService.uploadByForm(file, filePath, fileName,module, "", useOriName));
        } catch (Exception e) {
            logger.error("upload err", e);
            return ErrorTip.create(BusinessCode.UploadFileError);
        }
    }
}
