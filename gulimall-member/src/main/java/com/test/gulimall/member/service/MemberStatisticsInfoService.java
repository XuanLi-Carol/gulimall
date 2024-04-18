package com.test.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.test.common.utils.PageUtils;
import com.test.gulimall.member.entity.MemberStatisticsInfoEntity;

import java.util.Map;

/**
 * 会员统计信息
 *
 * @author CarolLi
 * @email xuanli.carol@gmail.com
 * @date 2024-03-19 10:01:12
 */
public interface MemberStatisticsInfoService extends IService<MemberStatisticsInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

