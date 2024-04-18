package com.test.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.test.common.validator.ListValue;
import com.test.common.validator.group.AddGroup;
import com.test.common.validator.group.UpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author CarolLi
 * @email xuanli.carol@gmail.com
 * @date 2024-03-18 18:11:42
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@Null(message = "新增时不能指定brandId",groups = {AddGroup.class})
	@NotNull(message = "修改时必须指定brandId",groups = {UpdateGroup.class})
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名称不能为空",groups = {AddGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@URL(message = "logo的地址必须是合法的URL",groups = {AddGroup.class, UpdateGroup.class})
	@NotBlank(message = "新增时必须指定logo",groups = {AddGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@ListValue(vals = {0,1},groups = {AddGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@Pattern(regexp = "[a-zA-Z]",message = "首字母必须是a-z或者A-Z",groups = {AddGroup.class, UpdateGroup.class})
	@NotEmpty(groups = {AddGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@Min(value = 0,message = "排序字段必须大于等于零",groups = {AddGroup.class, UpdateGroup.class})
	@NotNull(groups = {AddGroup.class})
	private Integer sort;

}
