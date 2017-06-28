package com.github.dataflow.core.instance.handler;

import com.github.dataflow.core.exception.InstanceException;
import com.github.dataflow.sender.core.DataSenderManager;
import com.github.dataflow.core.store.DataStore;
import com.github.dataflow.core.transformer.GroovyShellDataTransformer;
import com.github.dataflow.dubbo.model.DataInstance;
import com.github.dataflow.dubbo.model.DataOutputMapping;
import com.github.dataflow.dubbo.model.DataTable;
import com.github.dataflow.sender.core.DataSender;
import com.github.dataflow.sender.core.DataSenderHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author kevin
 * @date 2017-05-30 1:13 AM.
 */
public abstract class AbstractInstanceHandler implements ApplicationContextAware, InitializingBean {
    /**
     * zk集群地址
     */
    @Value("${node.zookeeper.addresses}")
    private String zookeeperAddresses;

    protected List<DataSenderHandler> dataSenderHandlers = new ArrayList<>();


    private ApplicationContext applicationContext;

    public void afterPropertiesSet() throws Exception {
        dataSenderHandlers.clear();
        Map<String, DataSenderHandler> dataSenderHandlerMap = this.applicationContext.getBeansOfType(DataSenderHandler.class);
        if (CollectionUtils.isEmpty(dataSenderHandlerMap)) {
            throw new InstanceException("Not found any DataSenderHandler.");
        } else {
            dataSenderHandlers.addAll(dataSenderHandlerMap.values());
        }
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    protected DataStore buildDataStore(DataInstance dataInstance) {
        List<DataOutputMapping> dataOutputMappings = dataInstance.getDataOutputMappings();
        if (CollectionUtils.isEmpty(dataOutputMappings)) {
            throw new InstanceException("dataOutputMappings must not be empty.");
        }

        // transform
        DataStore dataStore = doBuildDataStore();
        if (!StringUtils.isEmpty(dataInstance.getTransformScript())) {
            dataStore.setDataTransformer(new GroovyShellDataTransformer(dataInstance.getTransformScript()));
        }

        // filter
        Map<String, Map<String, List<String>>> columnsToFilterMap = buildColumnsToFilterMap(dataInstance);
        if (!CollectionUtils.isEmpty(columnsToFilterMap)) {
            dataStore.setColumnsToFilterMap(columnsToFilterMap);
        }

        // sender
        initDataSender(dataOutputMappings, dataStore);
        return dataStore;
    }

    protected abstract DataStore doBuildDataStore();

    private Map<String, Map<String, List<String>>> buildColumnsToFilterMap(DataInstance dataInstance) {
        if (!CollectionUtils.isEmpty(dataInstance.getDataTables())) {
            Map<String, Map<String, List<String>>> columnsToFilterMap = new HashMap<>();
            for (DataTable dataTable : dataInstance.getDataTables()) {
                Map<String, List<String>> columnsMap = columnsToFilterMap.get(dataTable.getSchemaName());
                if (columnsMap == null) {
                    columnsMap = new HashMap<>();
                    columnsToFilterMap.put(dataTable.getSchemaName(), columnsMap);
                }

                columnsMap.put(dataTable.getTableName(), columnsToList(dataTable.getColumns()));
            }
            return columnsToFilterMap;
        } else {
            return new HashMap<>();
        }
    }

    private List<String> columnsToList(String columns) {
        return Arrays.asList(columns.split(","));
    }

    protected void initDataSender(List<DataOutputMapping> dataOutputMappings, DataStore dataStore) {
        Map<String, DataSender> dataSenderMap = new HashMap<>();
        for (DataOutputMapping dataOutputMapping : dataOutputMappings) {
            DataSender dataSender = dataSenderMap.get(dataOutputMapping.getSchemaName());
            if (dataSender == null) {
                // 输出数据源的id就是DataSender的标识符
                Long dataSenderId = dataOutputMapping.getDataSourceOutput().getId();
                dataSender = DataSenderManager.get(dataSenderId);
                if (dataSender == null) {
                    dataSender = createDataSender(dataOutputMapping);
                    if (dataSender.isSingleton()) {
                        DataSenderManager.put(dataSenderId, dataSender);
                    }
                    dataSender.setDataSenderId(dataSenderId);
                }
                dataSenderMap.put(dataOutputMapping.getSchemaName(), dataSender);
            }
        }

        dataStore.setDataSenderMap(dataSenderMap);
    }

    protected DataSender createDataSender(DataOutputMapping dataOutputMapping) {
        Integer type = dataOutputMapping.getDataSourceOutput().getType();
        for (DataSenderHandler dataSenderHandler : dataSenderHandlers) {
            if (dataSenderHandler.support(type)) {
                try {
                    return dataSenderHandler.doCreateDataSender(dataOutputMapping);
                } catch (Exception e) {
                    throw new InstanceException(e);
                }
            }
        }

        throw new InstanceException("there is no DataSenderHandler support the type [" + type + "] of DataOutputMapping + [" + dataOutputMapping + "].");
    }

    ;

    public String getZookeeperAddresses() {
        return zookeeperAddresses;
    }

    public void setZookeeperAddresses(String zookeeperAddresses) {
        this.zookeeperAddresses = zookeeperAddresses;
    }
}