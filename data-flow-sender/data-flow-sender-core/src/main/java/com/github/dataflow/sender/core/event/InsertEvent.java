package com.github.dataflow.sender.core.event;

import com.github.dataflow.common.model.RowMetaData;

/**
 * @author : kevin
 * @version : Ver 1.0
 * @description :
 * @date : 2017/7/7
 */
public interface InsertEvent {
    RowMetaData.EventType INSERT = RowMetaData.EventType.INSERT;
}
