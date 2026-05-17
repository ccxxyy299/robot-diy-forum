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
     * @param dto 标签信息
     */
    void add(TagDTO dto);

    /**
     * 删除标签
     * @param id 标签ID
     */
    void delete(Long id);

    /**
     * 修改标签
     * @param dto 标签信息
     * @param id 标签ID
     */
    void update(Long id, TagDTO dto);

    /**
     * 查询所有标签
     * @return 标签列表
     */
    List<TagVO> selectAll();

    /**
     * 根据标签名称模糊搜索
     * @param name 标签名称
     * @return 标签列表
     */
    List<TagVO> searchByName(String name);
}