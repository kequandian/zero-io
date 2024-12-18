package com.jfeat.fs.dto.resp;

import lombok.Data;

@Data
public class UploadResp {
    private String fullPath;
    private String fileUrl;
    private String fileName;
    private Integer fileSize;
    private String contentType;

}
