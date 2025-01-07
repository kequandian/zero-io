package com.jfeat.fs.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.fs.dto.resp.UploadResp;
import com.jfeat.fs.model.FileInfo;
import com.jfeat.fs.service.LoadFileCodeService;
import com.jfeat.fs.util.ImageUtil;
import io.minio.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Created by jackyhuang on 2018/1/3.
 */
@Service
public class LoadFileCodeServiceImpl implements LoadFileCodeService {

    protected final static Logger logger = LoggerFactory.getLogger(LoadFileCodeService.class);

    final static String DEFAULT_BUCKET_IMAGES = "images";
    final static String DEFAULT_BUCKET_ATTACHMENTS = "attachments";
    final static String DEFAULT_BUCKET_DOCS = "docs";

    private static Cache<String, String> cache = CacheBuilder
            .newBuilder()
            .maximumSize(100)
            .expireAfterAccess(60, TimeUnit.SECONDS) //当缓存项在指定的时间段内没有被读或写就会被回收。
            .build();

    @Value("${io.minio.export-endpoint}")
    private String exportEndpoint;
    @Value("${io.minio.endpoint}")
    private String minioUrl;
    @Value("${io.minio.access-key}")
    private String minioAccessKey;
    @Value("${io.minio.secret-key}")
    private String minioSecretKey;

    private static MinioClient minioClient;

    @PostConstruct
    public void init() {
        try {
            logger.info("init minio start minioUrl={},minioAccessKey={},minioSecretKey={}", minioUrl, minioAccessKey, minioSecretKey);
            minioClient =  MinioClient.builder()
                    .endpoint(minioUrl)
                    .credentials(minioAccessKey, minioSecretKey)
                    .build();
            logger.info("init minio success");
        } catch (Exception e) {
            logger.error("init minio fail={}", e.getMessage());
            throw e;
        }
    }

    @Override
    public String genAndGetCode(String name) {
        String key = UUID.randomUUID().toString();
        cache.put(key, name);
        return key;
    }

    @Override
    public boolean checkCode(String code) {
        try {

            String name = cache.get(code, () -> "");

            if (name == null || name.equals("")) {
                return false;
            }

            return true;
        } catch (ExecutionException e) {
            return false;
        }
    }

    // public static void main(String[] args){
    //     String[] imageExt = new String[] {"jpg", "jpeg", "png"};
    //     String[] zipExt = new String[] {"zip", "gzip", "bz2", "tar", "7z", "rar"};
    //     String[] docExt = new String[] {"doc", "docx", "ppt", "xlsx", "xls"};

    //     System.out.println(String.join(",", String.join(",", imageExt), String.join(",", zipExt), String.join(",", docExt)));
    // }


    @Override
    public FileInfo uploadFile(MultipartFile file, String fileSavePath, String bucket, String appid, String fileHost) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String extensionName = FilenameUtils.getExtension(originalFileName);
        if(StringUtils.isEmpty(extensionName)){
            throw new BusinessException(BusinessCode.BadRequest,  "上传文件需要带后缀文件类型！");
        }

        // if(extensionName != null){
        //     if(extensionName.equals("exe")||extensionName.equals("java")||extensionName.equals("jsp")||extensionName.equals("php")||extensionName.equals("asp")){
        //         throw new BusinessException(BusinessCode.BadRequest,  "文件类型有误! 不能为：" + extensionName +"类型的文件");
        //     }
        // }
        {
            String[] imageExt = new String[] {"jpg", "jpeg", "png"};
            String[] zipExt = new String[] {"zip", "gzip", "bz2", "tar", "7z", "rar"};
            String[] docExt = new String[] {"doc", "docx", "xls", "xlsx", "ppt","pptx"};

            if(Stream.of(imageExt).anyMatch(ext->ext.equals(extensionName)) || Stream.of(zipExt).anyMatch(ext->ext.equals(extensionName)) || Stream.of(docExt).anyMatch(ext->ext.equals(extensionName))){
                // pass
            }else{
                throw new BusinessException(BusinessCode.BadRequest,  "仅支持有限的文件类型：" + String.join(",", String.join(",", imageExt), String.join(",", zipExt), String.join(",", docExt)));
            }

            if(StringUtils.isEmpty(bucket)) {
                if (Stream.of(imageExt).anyMatch(ext -> ext.equals(extensionName))) {
                    bucket = DEFAULT_BUCKET_IMAGES;
                } else if (Stream.of(zipExt).anyMatch(ext -> ext.equals(extensionName))) {
                    bucket = DEFAULT_BUCKET_ATTACHMENTS;
                } else if (Stream.of(docExt).anyMatch(ext -> ext.equals(extensionName))) {
                    bucket = DEFAULT_BUCKET_DOCS;
                }
            }
        }

        Long fileSize = file.getSize();
        String fileName = UUID.randomUUID() + "." + extensionName;
        // just ensure fileSavePath exists
        {
            File fileSaveFile = new File(fileSavePath);
            if (!fileSaveFile.exists()) {
                fileSaveFile.mkdirs();
            }
        }

        // check bucket exists, cos's required authorized.
        if ((!StringUtils.isEmpty(bucket)) || (!StringUtils.isEmpty(appid))) {
            String targetPath = String.join(File.separator, fileSavePath, bucket);
            File bucketFile = new File(targetPath);
            Assert.isTrue(bucketFile.exists(), "path from (X-FS-BUCKET) not exists: " + bucketFile.getPath());
        }

        // get current year
        String currentYear = new SimpleDateFormat("yyyy").format(new Date());
        // just ensure targetFilePath exists
        {
            String targetFilePath = String.join(File.separator, fileSavePath, bucket, appid, currentYear);
            File tmpFileSaveFile = new File(targetFilePath);
            if (!tmpFileSaveFile.exists()) {
                tmpFileSaveFile.mkdirs();
            }
        }
        File targetFile = new File(String.join(File.separator, fileSavePath, bucket, appid, currentYear, fileName));

        //boolean readable = target.setReadable(true);
        //if(readable){
        logger.info("file uploading to: {}", targetFile.getAbsolutePath());
        FileUtils.copyInputStreamToFile(file.getInputStream(), targetFile);
        logger.info("file uploaded to: {}", targetFile.getAbsolutePath());
            /*}else{
                throw new BusinessException(BusinessCode.UploadFileError, "file is not readable");
            }*/

        // get relative path
        String relativePath = targetFile.getAbsolutePath().substring(new File("./").getAbsolutePath().length() - 1);
        String pathFileName = String.join(File.separator, appid, currentYear, fileName);
        return FileInfo.create(fileHost, bucket, pathFileName, extensionName, originalFileName, fileSize, relativePath);
    }

    @Override
    public FileInfo upload64(String base64Data, Boolean blur, String fileSavePath, String bucket, String appid, String fileHost) throws IOException{
        String dataPrix = "";
        String data = "";
        String[] d = base64Data.split("base64,");
        if (d != null && d.length == 2) {
            dataPrix = d[0];
            data = d[1];
        } else {
            throw new BusinessException(BusinessCode.UploadFileError);
        }

        String suffix = "";
        if ("data:image/jpeg;".equalsIgnoreCase(dataPrix)) {
            suffix = ".jpg";
        } else if ("data:image/x-icon;".equalsIgnoreCase(dataPrix)) {
            suffix = ".ico";
        } else if ("data:image/gif;".equalsIgnoreCase(dataPrix)) {
            suffix = ".gif";
        } else if ("data:image/png;".equalsIgnoreCase(dataPrix)) {
            suffix = ".png";
        } else {
            throw new BusinessException(BusinessCode.UploadFileError);
        }

        byte[] dataBytes = Base64Utils.decodeFromString(data);

        String pictureName = UUID.randomUUID().toString() + suffix;
        String blurryName = "";

        String targetPath = String.join(File.separator, fileSavePath, bucket, appid, pictureName);
        File target = new File(targetPath);

        target.setReadable(true);
        FileUtils.writeByteArrayToFile(target, dataBytes);
        logger.info("file uploaded to: {}", target.getAbsolutePath());
        File reducedFile = ImageUtil.reduce(target);
        logger.info("file reduced to: {}", reducedFile.getAbsolutePath());
        pictureName = reducedFile.getName();

        File thumbFile = ImageUtil.thumb(reducedFile);
        logger.info("file thumb to: {}", thumbFile.getAbsoluteFile());

        if (blur) {
            File blurryFile = ImageUtil.reduce(ImageUtil.gaos(reducedFile));
            blurryFile.setReadable(true);
            blurryName = blurryFile.getName();

            File blurryThumbFile = ImageUtil.thumb(blurryFile);
            logger.info("blurry file thumb to: {}", blurryThumbFile.getAbsoluteFile());
        }

        return FileInfo.create(fileHost, pictureName, blurryName);
    }

    /**
     * 解析路径，返回 bucketName 和 objectName
     * @param path
     * @return
     */
    private String[] processPath(String path) {
        //第二个 ‘/’ 下标
        int index = path.indexOf("/", 1);
        if(index == -1) {
            return null;
        }
        String bucketName = path.substring(1, index);
        String objectPath = path.substring(index + 1);
        return new String[]{bucketName, objectPath};
    }

    private UploadResp upload(InputStream in, String bucketName,String objectPath, String objectName, String contentType) {
        String object = objectPath + objectName;
        try {
            BucketExistsArgs existsArgs = BucketExistsArgs.builder().bucket(bucketName).build();
            boolean isExists = minioClient.bucketExists(existsArgs);
            if (!isExists) {
                MakeBucketArgs makeArgs = MakeBucketArgs.builder().bucket(bucketName).build();
                minioClient.makeBucket(makeArgs);
            }
        } catch (Exception e) {
            logger.error("check and create bucket fail:{}", e.getMessage());
            throw new BusinessException(BusinessCode.BadRequest,  "检查并创建文件桶失败！");
        }

        UploadResp uploadResp = new UploadResp();
        try {
            long partSize = 64L * 1024L * 1024L; // 分片上传，每个分片64MB
            int fileSize = in.available();
            PutObjectArgs putObjectArgs = PutObjectArgs.builder().bucket(bucketName)
                    .object(object).stream(in, fileSize, partSize)
                    .contentType(contentType)
                    .build();
            minioClient.putObject(putObjectArgs);

            String fullPath = "/" + bucketName + "/"+ object;
            uploadResp.setContentType(contentType);
            uploadResp.setFileSize(fileSize / 1024);
            uploadResp.setFileName(objectName);
            uploadResp.setFullPath(fullPath);
            uploadResp.setFileUrl(exportEndpoint+fullPath);
        } catch (Exception e) {
            logger.error("upload file fail:{}", e.getMessage());
            throw new BusinessException(BusinessCode.BadRequest,  "上传文件失败！");
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                logger.error("close err", e);
            }
        }
        return  uploadResp;
    }

    @Override
    public UploadResp uploadByForm(MultipartFile file, String filePath, String fileName, String module, String userId) {
        if(!StringUtils.startsWithIgnoreCase(filePath, "/")) {
            throw new BusinessException(BusinessCode.BadRequest,  "文件路径需要/开头");
        }
        if(!StringUtils.endsWithIgnoreCase(filePath, "/")) {
            throw new BusinessException(BusinessCode.BadRequest,  "文件路径需要/结尾");
        }
        String[] bucketAndObject = processPath(filePath);
        if(bucketAndObject == null) {
            logger.info("file upload file path err {}",filePath);
            throw new BusinessException(BusinessCode.BadRequest,  "文件路径错误！");
        }

        if(file.isEmpty()) {
            throw new BusinessException(BusinessCode.BadRequest,  "文件不可为空！");
        }

        String bucketName = bucketAndObject[0];
        String objectPath = bucketAndObject[1];
        String realFileName = file.getOriginalFilename();
        String suffix = "";
        if(realFileName.lastIndexOf(".") > 0) {
            suffix = realFileName.substring(realFileName.lastIndexOf("."));
        }
        if(StringUtils.isEmpty(fileName)) {
            fileName = StringUtils.replace(UUID.randomUUID().toString(),"-","") + suffix;
        } else if(!fileName.contains(".")) {
            fileName = fileName + suffix;
        }

        // 考虑存储数据库
        logger.info("file upload  realFileName={} fileName:{} module:{} userId:{}", realFileName, fileName, module, userId);
        try {
            return upload(file.getInputStream(),bucketName,objectPath,fileName,file.getContentType());
        } catch (Exception e) {
            logger.error("upload file fail:{}", e.getMessage());
            throw new BusinessException(BusinessCode.BadRequest,  "上传文件失败！");
        }
    }

    @Override
    public UploadResp uploadBytes(byte[] bytes, String fileSuffix, String contentType, String filePath, String fileName, String module, String userId) {
        if(!StringUtils.startsWithIgnoreCase(filePath, "/")) {
            throw new BusinessException(BusinessCode.BadRequest,  "文件路径需要/开头");
        }
        if(!StringUtils.endsWithIgnoreCase(filePath, "/")) {
            throw new BusinessException(BusinessCode.BadRequest,  "文件路径需要/结尾");
        }

        ByteArrayInputStream dataInputStream = null;
        try {
            dataInputStream = new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            throw new BusinessException(BusinessCode.BadRequest,  "字符转utf-8错误");
        }

        String[] bucketAndObject = processPath(filePath);
        if(bucketAndObject == null) {
            logger.info("file upload file path err {}",filePath);
            throw new BusinessException(BusinessCode.BadRequest,  "文件路径错误！");
        }

        String bucketName = bucketAndObject[0];
        String objectPath = bucketAndObject[1];
        if(StringUtils.isEmpty(fileName)) {
            fileName = StringUtils.replace(UUID.randomUUID().toString(),"-","") + "." + fileSuffix;
        }

        // 考虑存储数据库
        logger.info("file upload fileName:{} module:{} userId:{}", fileName, module, userId);
        try {
            return upload(dataInputStream, bucketName, objectPath, fileName, contentType);
        } catch (Exception e) {
            logger.error("upload file fail:", e);
            throw new BusinessException(BusinessCode.BadRequest,  "上传文件失败！");
        }
    }


    @Override
    public Boolean delete(String filePath, String userId) {
        String[] bucketAndObject = processPath(filePath);
        if(bucketAndObject == null) {
            logger.info("file upload file path err {}", filePath);
            throw new BusinessException(BusinessCode.GeneralIOError);
        }

        String bucketName = bucketAndObject[0];
        String objectPath = bucketAndObject[1];
        //根据objectName删除minio的数据
        try {
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket(bucketName)
                    .object(objectPath)
                    .build();
            minioClient.removeObject(removeObjectArgs);
        } catch (Exception e) {
            logger.error("删除minio文件失败={}" , e.getMessage(), e);
            throw new BusinessException(BusinessCode.GeneralIOError);
        }
        return true;
    }
}
