/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.hub.data;

import java.io.File;
import java.lang.Override;
import java.util.HashMap;
import java.util.Map;

import com.whizzosoftware.hobson.api.data.DataStreamField;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.api.data.DataStream;
import com.whizzosoftware.hobson.api.util.Constants;
import com.whizzosoftware.hobson.api.variable.VariableNotFoundException;
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

    private LocalTelemetryManager telemetryManager;
    private boolean firstRun = true;

    public DataPlugin(String pluginId) {
        super(pluginId);
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
        this.telemetryManager = new LocalTelemetryManager(this, new MapDBDataStreamStore(getDataFile("dataStreams")), new RRD4JDataStreamDB());

        // add the local telemetry manager
        getHubManager().getLocalManager().addTelemetryManager(telemetryManager);

        // set the status to running
        setStatus(new PluginStatus(PluginStatus.Code.RUNNING));
    }

    @Override
    public void onShutdown() {
    }

    @Override
    protected TypedProperty[] createSupportedProperties() {
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
            for (DataStream ds : telemetryManager.getDataStreams(Constants.DEFAULT_USER)) {
                logger.trace("Processing data stream {}", ds.getId());

                // build a map of variable values
                Map<String, Object> data = new HashMap<>();
                for (DataStreamField df : ds.getFields()) {
                    try {
                        data.put(df.getId(), getVariableManager().getVariable(df.getVariable()).getValue());
                    } catch (VariableNotFoundException e) {
                        logger.error("Skipping unpublished variable " + df.getVariable() + " in data stream " + ds.getId(), e);
                    }
                }

                // add to telemetry manager
                telemetryManager.addData(Constants.DEFAULT_USER, ds.getId(), now, data);
            }
        // defer to next run
        } else {
            firstRun = false;
        }
    }

    /**
     * Callback method when the plugin's configuration changes.
     *
     * @param config the new configuration
     */
    @Override
    public void onPluginConfigurationUpdate(PropertyContainer config) {
    }

    @Override
    public File getTelemetryDataFile(String userId, String dataStreamId) {
        return getDataFile(userId + "-" + dataStreamId + ".rrdb");
    }
}
