package com.jfeat.fs.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;


@Data
@ApiModel
public class UploadByTextReq implements Serializable {

    @ApiModelProperty(value ="文件名(aa.jpg) 必填", required = true)
    @NotNull(message = "文件名不能为空")
    private String fileName;

    @ApiModelProperty(value ="功能模块 方便定位问题", required = true)
    @NotNull(message = "模块不能为空")
    private String module;

    @ApiModelProperty(value ="文件路径 /images/head/ 必填", required = true)
    @NotNull(message = "文件路径不能为空")
    private String filePath;

    @ApiModelProperty(value = "文本",required = true)
    @NotNull(message = "文本不能为空")
    private String text;
}
