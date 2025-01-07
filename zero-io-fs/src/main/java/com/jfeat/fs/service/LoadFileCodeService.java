package com.jfeat.fs.service;

import com.jfeat.fs.model.FileInfo;
import com.jfeat.fs.dto.resp.UploadResp;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Created by jackyhuang on 2018/1/3.
 */
public interface LoadFileCodeService {

    /**
     * 生成下载码
     * @param name
     * @return
     */
    String genAndGetCode(String name);

    /**
     * 检查下载码，安全检查
     * @param code
     * @return
     */
    boolean checkCode(String code);

    /**
     * 上传文件服务
     * @param file
     * @param fileHost
     * @param fileSavePath
     * @param bucket
     * @return
     */
    FileInfo uploadFile(MultipartFile file, String fileSavePath, String bucket, String appid, String fileHost) throws IOException;


    /**
     * 上传图片数据流
     * @param base64Data
     * @param fileSavePath
     * @param bucket
     * @param fileHost
     * @param appid
     * @return
     * @throws IOException
     */
    FileInfo upload64(String base64Data, Boolean blur, String fileSavePath, String bucket, String appid, String fileHost) throws IOException;

    /**
     * 通用表单上传文件
     * @param file
     * @param filePath
     * @param fileName
     * @param module 接入的模块 可选
     * @param userId 用户标识 可选
     * @return UploadResp
     */
    UploadResp uploadByForm(MultipartFile file, String filePath, String fileName, String module, String userId);

    /**
     * 通用文本上传
     * @param bytes
     * @param fileSuffix
     * @param contentType
     * @param filePath
     * @param fileName
     * @param module 接入的模块 可选
     * @param userId 用户标识 可选
     * @return UploadResp
     */
    UploadResp uploadBytes(byte[] bytes, String fileSuffix, String contentType, String filePath, String fileName, String module, String userId);

    /**
     * 通用删除接口
     * @param filePath
     * @param userId
     * @return
     */
    Boolean delete(String filePath, String userId);

}
