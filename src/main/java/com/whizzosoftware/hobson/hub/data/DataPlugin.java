/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.hub.data;

import java.io.File;
import java.lang.Override;
import java.util.HashMap;
import java.util.Map;

import com.whizzosoftware.hobson.api.data.DataStreamField;
import com.whizzosoftware.hobson.api.hub.HubContext;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.api.data.DataStream;
import com.whizzosoftware.hobson.api.variable.DeviceVariableContext;
import com.whizzosoftware.hobson.hub.data.db.RRD4JDataStreamDB;
import com.whizzosoftware.hobson.hub.data.store.MapDBDataStreamStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whizzosoftware.hobson.api.plugin.AbstractHobsonPlugin;
import com.whizzosoftware.hobson.api.plugin.PluginStatus;

/**
 * A plugin that interrogates devices configured to provide data and stores the appropriate variable values locally every 5 mins.
 *
 * @author Dan Noguerol
 */
public class DataPlugin extends AbstractHobsonPlugin implements FileProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private LocalDataStreamManager dataStreamManager;
    private boolean firstRun = true;

    public DataPlugin(String pluginId, String version, String description) {
        super(pluginId, version, description);
    }

    /**
     * Returns the plugin name.
     *
     * @return a String
     */
    @Override
    public String getName() {
        return "Data Plugin";
    }

    @Override
    public void onStartup(PropertyContainer config) {
        this.dataStreamManager = new LocalDataStreamManager(this, new MapDBDataStreamStore(getDataFile("dataStreams")), new RRD4JDataStreamDB());

        // add the local data stream manager
        getHubManager().getLocalManager().addDataStreamManager(dataStreamManager);

        // set the status to running
        setStatus(new PluginStatus(PluginStatus.Code.RUNNING));

        logger.info("Data plugin started!");
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public void onPluginConfigurationUpdate(PropertyContainer config) {
    }

    @Override
    protected TypedProperty[] getConfigurationPropertyTypes() {
        return null;
    }

    @Override
    public long getRefreshInterval() {
        return 300;
    }

    @Override
    public void onRefresh() {
        // we don't want to run immediately since plugins may still be starting their devices
        if (!firstRun) {
            long now = System.currentTimeMillis();
            for (DataStream ds : dataStreamManager.getDataStreams(HubContext.createLocal())) {
                logger.trace("Processing data stream {}", ds.getId());

                // build a map of variable values
                Map<String, Object> data = new HashMap<>();
                for (DataStreamField df : ds.getFields()) {
                    DeviceVariableContext dvctx = df.getVariable();
                    data.put(df.getId(), getDeviceVariableState(dvctx).getValue());
                }

                // add to data stream manager
                dataStreamManager.addData(HubContext.createLocal(), ds.getId(), now, data);
            }
        // defer to next run
        } else {
            firstRun = false;
        }
    }

    @Override
    public File getDataStreamDataFile(HubContext ctx, String dataStreamId) {
        return getDataFile(ctx.getHubId() + "-" + dataStreamId + ".rrdb");
    }
}
