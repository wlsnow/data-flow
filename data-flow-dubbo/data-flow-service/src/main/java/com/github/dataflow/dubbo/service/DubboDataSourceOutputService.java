package com.github.dataflow.dubbo.service;

import com.github.dataflow.dubbo.model.DataSourceOutput;
import com.github.dataflow.dubbo.model.PageSet;
import com.github.dataflow.dubbo.model.ServiceResult;

import java.util.List;

/**
 * @author : kevin
 * @version : Ver 1.0
 * @description :
 * @date : 2017/6/2
 */
public interface DubboDataSourceOutputService {
    /**
     * 新增条目
     *
     * @param dataSourceOutput 带有新增数据的对象
     * @return 返回新增条目数据库影响数
     */
    ServiceResult<Long> insert(DataSourceOutput dataSourceOutput);

    /**
     * 更新条目
     *
     * @param dataSourceOutput 带有更新数据的对象
     * @return 返回更新条目数据库影响数
     */
    ServiceResult<Integer> update(DataSourceOutput dataSourceOutput);

    /**
     * 根据ID删除对应条目
     *
     * @param id id
     * @return 返回删除条目数据库影响数
     */
    ServiceResult<Integer> delete(Long id);

    /**
     * 根据ID查询对应的条目
     *
     * @param id id
     * @return 返回查询的结果
     */
    ServiceResult<DataSourceOutput> getById(Long id);

    /**
     * 根据条件查询对应的条目总数
     *
     * @param dataSourceOutput 带有查询条件的对象
     * @return 返回查询的结果总数
     */
    ServiceResult<Long> countByCondition(DataSourceOutput dataSourceOutput);

    /**
     * 根据条件查询对应的条目
     *
     * @param dataSourceOutput 带有查询条件的对象
     * @param pageSet          分页对象
     * @return 返回查询的结果集合
     */
    ServiceResult<List<DataSourceOutput>> findByCondition(DataSourceOutput dataSourceOutput, PageSet pageSet);

}
