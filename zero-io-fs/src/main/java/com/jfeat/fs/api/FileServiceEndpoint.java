package com.jfeat.fs.api;

import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.crud.base.tips.ErrorTip;
import com.jfeat.crud.base.tips.SuccessTip;
import com.jfeat.crud.base.tips.Tip;
import com.jfeat.crud.base.util.StrKit;
import com.jfeat.fs.dto.resp.UploadResp;
import com.jfeat.fs.properties.FSProperties;
import com.jfeat.fs.service.LoadFileCodeService;
import com.jfeat.fs.model.FileInfo;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Created by jackyhuang on 2017/7/4.
 * appid and bucket must be prepared during deploy
 */
@Api(value = "fs-upload")
@RestController
public class FileServiceEndpoint {

    protected final static Logger logger = LoggerFactory.getLogger(FileServiceEndpoint.class);

    @Autowired
    FSProperties FSProperties;

    @Autowired
    LoadFileCodeService loadFileCodeService;

    @ApiOperation(value = "上传文件（下个版本废弃，不要再使用！）", response = FileInfo.class)
    @PostMapping("/api/fs/uploadfile")
    public Tip fileUpload(@RequestHeader(value = "authorization", required = false) String token,
                          @ApiParam("上传文件至不同的分组") @RequestHeader(value = "X-FS-BUCKET", required = false, defaultValue = "") String bucket,
                          @ApiParam("不同应用上传文件至独立目录") @RequestHeader(value = "X-FS-APPID", required = false, defaultValue = "") String appid,
                          @RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(BusinessCode.BadRequest, "file is empty");
        }
        String fileHost = FSProperties.getFileHost();
        String fileSavePath = FSProperties.getFileUploadPath();

        try {
            logger.info("============== upload start ===============");
            FileInfo fileInfo = loadFileCodeService.uploadFile(file, fileSavePath, bucket, appid, fileHost);
            return SuccessTip.create(fileInfo);

        } catch (Exception e) {
            logger.info("============== exception {} ===============");
            logger.info(e.getMessage());
            logger.info(e.getLocalizedMessage());
//            logger.info(e.toString());
            throw new BusinessException(BusinessCode.UploadFileError);
        }
    }

    /**
     * 数据格式
     * data:image/jpeg;base64,/9j/4AAQSkZJRgABAQ
     *
     * @return
     * @throws IOException
     */
    @ApiOperation(value = "Base64格式上传图片 （下个版本废弃，不要再使用！）", response = FileInfo.class)
    @PostMapping("/api/fs/upload64")
    @ResponseBody
    public Tip base64Upload(@RequestHeader(value = "authorization", required = false) String token,
                            @ApiParam("上传文件至不同的分组") @RequestHeader(value = "X-FS-BUCKET", required = false, defaultValue = "") String bucket,
                            @ApiParam("不同应用上传文件至独立目录") @RequestHeader(value = "X-FS-APPID", required = false, defaultValue = "") String appid,
                            @RequestParam(name = "blur", defaultValue = "false") Boolean blur, HttpServletRequest request) throws IOException {

        String base64Data = IOUtils.toString(request.getInputStream(), "UTF-8");
        if (StrKit.isBlank(base64Data)) {
            throw new BusinessException(BusinessCode.UploadFileError);
        }
        String fileHost = FSProperties.getFileHost();
        String fileSavePath = FSProperties.getFileUploadPath();

        try {
            logger.info("============== upload start ===============");
            FileInfo fileInfo = loadFileCodeService.upload64(base64Data, blur, fileSavePath, bucket, appid, fileHost);
            return SuccessTip.create(fileInfo);

        } catch (Exception e) {
            logger.info("============== exception {} ===============");
            logger.info(e.getMessage());
            logger.info(e.getLocalizedMessage());
            throw new BusinessException(BusinessCode.UploadFileError);
        }
    }

    @ApiOperation(value = "通用表单上传文件", response = UploadResp.class)
    @PostMapping("/api/adm/fs/uploadByForm")
    public Tip admUploadByForm(@RequestParam MultipartFile file,
                          @RequestParam @ApiParam(value = "文件路径 /images/head/", required = true) String filePath,
                          @RequestParam(required = false) @ApiParam("文件名（例如：aa.jpg 没后缀服务端使用文件后缀）可选 为空使用uuid") String fileName,
                          @RequestParam(required = false) @ApiParam("功能模块 方便定位问题") String module,
                          @RequestParam(required = false) @ApiParam("是否使用原文件名") Boolean useOriName) {
        try {
            return SuccessTip.create(loadFileCodeService.uploadByForm(file, filePath, fileName,module, "", useOriName));
        } catch (BusinessException e) {
            logger.error("upload1 err", e);
            return ErrorTip.create(e.getCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("upload2 err", e);
            return ErrorTip.create(BusinessCode.UploadFileError);
        }
    }

    @ApiOperation(value = "通用表单上传文件", response = UploadResp.class)
    @PostMapping("/api/fs/uploadByForm")
    public Tip uploadByForm(@RequestParam MultipartFile file,
                            @RequestParam @ApiParam(value = "文件路径 /images/head/", required = true) String filePath,
                            @RequestParam(required = false) @ApiParam("文件名（例如：aa.jpg 没后缀服务端使用文件后缀）可选 为空使用uuid") String fileName,
                            @RequestParam(required = false) @ApiParam("功能模块 方便定位问题") String module,
                            @RequestParam(required = false) @ApiParam("是否使用原文件名") Boolean useOriName) {
        try {
            return SuccessTip.create(loadFileCodeService.uploadByForm(file, filePath, fileName,module, "", useOriName));
        } catch (BusinessException e) {
            logger.error("upload1 err", e);
            return ErrorTip.create(e.getCode(), e.getMessage());
        }
        catch (Exception e) {
            logger.error("upload2 err", e);
            return ErrorTip.create(BusinessCode.UploadFileError);
        }
    }

    @ApiOperation(value = "通用字节数组上传", response = UploadResp.class)
    @PostMapping("/api/adm/fs/uploadBytes")
    public Tip uploadBytes(@RequestParam byte[] bytes,
                            @RequestParam(required = true) @ApiParam("文件后缀") String fileSuffix,
                            @RequestParam(required = false) @ApiParam("文件类型") String contentType,
                            @RequestParam @ApiParam(value = "文件路径 /images/head/", required = true) String filePath,
                            @RequestParam(required = false) @ApiParam("文件名（例如：aa.jpg 没后缀服务端使用文件后缀）可选 为空使用uuid") String fileName,
                            @RequestParam(required = true) @ApiParam("功能模块 方便定位问题") String module) {
        try {
            return SuccessTip.create(loadFileCodeService.uploadBytes(bytes, fileSuffix,contentType, filePath, fileName,module, ""));
        } catch (BusinessException e) {
            logger.error("upload err", e);
            return ErrorTip.create(e.getCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("upload err", e);
            return ErrorTip.create(BusinessCode.UploadFileError);
        }
    }

    @ApiOperation(value = "文件删除", response = Boolean.class)
    @GetMapping("/api/adm/fs/delete")
    public Tip delete(@RequestParam @ApiParam("文件路径 /images/head/jj.jpg") String fullPath) {
        return SuccessTip.create(loadFileCodeService.delete(fullPath, ""));
    }
}
