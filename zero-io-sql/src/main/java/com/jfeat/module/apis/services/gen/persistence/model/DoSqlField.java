package com.jfeat.module.apis.services.gen.persistence.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import com.baomidou.mybatisplus.annotation.TableId;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author Code generator
 * @since 2022-09-16
 */
@Data
@TableName("`lc`.`t_apis_dosql`")
@ApiModel(value = "DoSqlField对象", description = "")
public class DoSqlField extends Model<DoSqlField> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "接口名称")
    private String apiName;

    @ApiModelProperty(value = "sql文件路径")
    private String sqlFilePath;

    @ApiModelProperty(value = "参数")
    private String params;

    @ApiModelProperty(value = "备注")
    private String note;

    
    // DTO data 
    @ApiModelProperty(value = "sql内容")
    @TableField(exist = false)
    private String sql;
}
