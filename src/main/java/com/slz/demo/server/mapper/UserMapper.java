package com.slz.demo.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.slz.demo.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserMapper extends BaseMapper<User> {
}
