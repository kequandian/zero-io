package com.jfeat.fs.api;

import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.crud.base.tips.ErrorTip;
import com.jfeat.crud.base.tips.SuccessTip;
import com.jfeat.crud.base.tips.Tip;
import com.jfeat.crud.base.util.StrKit;
import com.jfeat.fs.properties.FSProperties;
import com.jfeat.fs.dto.resp.UploadResp;
import com.jfeat.fs.model.FileInfo;
import com.jfeat.fs.service.LoadFileCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
// import org.apache.commons.io.FileUtils;
// import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.util.Assert;
// import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
// import java.io.File;
import java.io.IOException;
// import java.util.UUID;

/**
 * Created by vincent on 2022/1/8.
 * appid and bucket must be prepared during deploy
 */
@Api(value = "fs-upload-user")
@RestController
public class UserEndFileServiceEndpoint {

    protected final static Logger logger = LoggerFactory.getLogger(UserEndFileServiceEndpoint.class);

    @Autowired
    FSProperties FSProperties;

    @Autowired
    LoadFileCodeService loadFileCodeService;

// updated in 2024-12-30 先暂时使用 1.0.0 版本的代码逻辑，避免影响目前已对接的服务
//    @ApiOperation(value = "上传文件", response = FileInfo.class)
//    @PostMapping("/api/u/fs/uploadfile")
//    public Tip fileUpload(@RequestHeader(value = "authorization", required = false) String token,
//                          @ApiParam("上传文件至不同的分组") @RequestHeader(value = "X-FS-BUCKET", required = false, defaultValue = "") String bucket,
//                          @ApiParam("不同应用上传文件至独立目录") @RequestHeader(value = "X-FS-APPID", required = false, defaultValue = "") String appid,
//                          @RequestPart("file") MultipartFile file) {
//        if (file.isEmpty()) {
//            throw new BusinessException(BusinessCode.BadRequest, "file is empty");
//        }
//        if (bucket == null) bucket = "";
//        String fileHost = FSProperties.getFileHost();
//        String fileSavePath = FSProperties.getFileUploadPath();
//
//        try {
//            logger.info("============== upload start ===============");
//            FileInfo fileInfo = loadFileCodeService.uploadFile(file, fileSavePath, bucket, appid, fileHost);
//            return SuccessTip.create(fileInfo);
//
//        } catch (Exception e) {
//            logger.info("============== exception {} ===============");
//            logger.info(e.getMessage());
//            logger.info(e.getLocalizedMessage());
//            logger.info(e.toString());
//            throw new BusinessException(BusinessCode.UploadFileError);
//        }
//    }

    @ApiOperation(value = "上传文件至桶", response = FileInfo.class)
    @PostMapping("/api/fs/upload/{bucket}")
    public Tip fileUploadWithBucket(@ApiParam("上传文件至不同的分组") @PathVariable("bucket") String bucket,
                          @RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(BusinessCode.BadRequest, "file is empty");
        }
        // String fileHost = FSProperties.getFileHost();
        String fileSavePath = FSProperties.getFileUploadPath();

        try {
            FileInfo fileInfo = loadFileCodeService.uploadFile(file, fileSavePath, bucket/*, fileHost*/);
            return SuccessTip.create(fileInfo);
            
        } catch (Exception e) {
            logger.info(e.toString());
            throw new BusinessException(BusinessCode.UploadFileError, e.toString());
        }
    }

    @ApiOperation(value = "上传文件", response = FileInfo.class)
    @PostMapping("/api/fs/uploadfile")
    public Tip fileUpload(@ApiParam("上传文件至不同的Bucket") @RequestHeader(value = "X-FS-BUCKET", required = false, defaultValue = "") String bucket,
                          @RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(BusinessCode.BadRequest, "file is empty");
        }
        String fileSavePath = FSProperties.getFileUploadPath();

        try {
            FileInfo fileInfo = loadFileCodeService.uploadFile(file, fileSavePath, bucket);
            return SuccessTip.create(fileInfo);
            
        } catch (Exception e) {
            logger.info(e.toString());
            throw new BusinessException(BusinessCode.UploadFileError, e.toString());
        }
    }


    @ApiOperation(value = "通过表单上传文件", response = UploadResp.class)
    @PostMapping("/api/fs/uploadform")
    public Tip uploadByForm(@RequestParam MultipartFile file,
                            @RequestParam @ApiParam(value = "文件路径 /images/head/", required = true) String filePath,
                            @RequestParam(required = false) @ApiParam("文件名（例如：aa.jpg 没后缀服务端使用文件后缀）可选 为空使用uuid") String fileName,
                            @RequestParam(required = false) @ApiParam("功能模块 方便定位问题") String module,
                            @RequestParam(required = false) @ApiParam("是否使用原文件名") Boolean useOriName) {
        try {
            return SuccessTip.create(loadFileCodeService.uploadByForm(file, filePath, fileName, module, "", useOriName));
        } catch (BusinessException e) {
            logger.error("upload1 err", e);
            return ErrorTip.create(e.getCode(), e.getMessage());
        }
        catch (Exception e) {
            logger.error("upload2 err", e);
            return ErrorTip.create(BusinessCode.UploadFileError);
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
}
