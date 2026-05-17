package com.slz.demo.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.slz.demo.pojo.dto.TagDTO;
import com.slz.demo.pojo.entity.ForumTag;
import com.slz.demo.pojo.vo.TagVO;

import java.util.List;

/**
 * 标签 Service
 */
public interface ForumTagService extends IService<ForumTag> {

    /**
     * 新增标签
     */
    void add(TagDTO dto);

    /**
     * 删除标签
     */
    void delete(Long id);

    /**
     * 修改标签
     */
    void update(Long id, TagDTO dto);

    /**
     * 查询所有标签
     */
    List<TagVO> selectAll();
}