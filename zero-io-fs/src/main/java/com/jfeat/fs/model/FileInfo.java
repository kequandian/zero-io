package com.jfeat.fs.model;


import com.jfeat.crud.base.util.StrKit;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.File;

/**
 * Created by jackyhuang on 2017/7/4.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class FileInfo {
    private String url;         //查对路径
    private String name;        //新的UUID命名的名称
    private String bucket;
    // private String host;
    private String originalFileName;
    private String extensionName;
    private Long size;
    private String path;

    //模糊图像名称
    private String blurryName;
    private String blurryUrl;


    public static FileInfo create(String url, String name) {
        return new FileInfo(url, name);
    }

    public static FileInfo create(String url, String name, String prefixUrl, String blurryName) {
        return new FileInfo(url, name, prefixUrl, blurryName);
    }

    public static FileInfo create(String url, String name, String extensionName, String originalFileName, Long size, String path) {
        return new FileInfo(url, null, name, extensionName, originalFileName, size, path);
    }

    public static FileInfo create(String url, String bucket, String name, String extensionName, String originalFileName, Long size, String path) {
        return new FileInfo(url, bucket, name, extensionName, originalFileName, size, path);
    }

    /**
     * Construction
     */

    public FileInfo(String url, String name) {
        this(url, null, name, null, null, 0L, null);
    }

    public FileInfo(String url, String name, String prefixUrl, String blurryName) {
        this(url, name);

        this.blurryName = blurryName;
        this.blurryUrl = prefixUrl + "/" + blurryName;
        if (StrKit.notBlank(prefixUrl) && prefixUrl.endsWith("/")) {
            this.blurryUrl = prefixUrl + blurryName;
        }
    }

    public FileInfo(String url, String name, String extensionName, String originalFileName, Long size, String path) {
        this(url, null, name, extensionName, originalFileName, size, path);
    }

    public FileInfo(String url, String bucket, String name, String extensionName, String originalFileName, Long size, String path) {
        // if(name.startsWith(File.separator)){
        //     name = name.substring(1);
        // }
        this.url = url;
        this.name = name;
        this.bucket = bucket;
        this.originalFileName = originalFileName;
        this.extensionName = extensionName;
        this.size = size;
        // if (bucket==null || bucket.equals("")){
        //     this.url = String.join(File.separator, host, name);
        // }else {
        //     this.url = String.join(File.separator, host, bucket, name);
        // }
        this.path = path;
    }


    // /**
    //  * Deprecated
    //  */
    // @Deprecated
    // public static FileInfo create(String host, String name, String extensionName, String originalFileName, Long size) {
    //     return new FileInfo(host, name, extensionName, originalFileName, size);
    // }

    // @Deprecated
    // public FileInfo(String host, String name, String extensionName, String originalFileName, Long size) {
    //     this(host, null, name, extensionName, originalFileName, size, null);
    // }

}
