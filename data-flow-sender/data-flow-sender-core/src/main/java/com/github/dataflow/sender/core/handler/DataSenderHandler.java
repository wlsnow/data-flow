package com.github.dataflow.sender.core.handler;

import com.github.dataflow.dubbo.model.DataOutputMapping;
import com.github.dataflow.sender.core.DataSender;

/**
 * @author kevin
 * @date 2017-05-30 1:01 AM.
 */
public interface DataSenderHandler {
    boolean support(int type);

    DataSender doCreateDataSender(DataOutputMapping dataSourceOutput) throws Exception;
}
