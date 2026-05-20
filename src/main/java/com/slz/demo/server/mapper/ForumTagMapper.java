package com.slz.demo.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.slz.demo.pojo.entity.ForumTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 标签 Mapper
 */
@Mapper
public interface ForumTagMapper extends BaseMapper<ForumTag> {

    /**
     * 查询未删除的同名标签数量
     * @param name 标签名
     */
    @Select("SELECT COUNT(*) FROM forum_tag WHERE name = #{name} AND is_deleted = 0")
    int countByName(@Param("name") String name);

    /**
     * 查询未删除的同名标签数量（排除指定ID）
     * @param name 标签名
     * @param id   标签ID
     */
    @Select("SELECT COUNT(*) FROM forum_tag WHERE name = #{name} AND id != #{id} AND is_deleted = 0")
    int countByNameExcludeId(@Param("name") String name, @Param("id") Long id);
}