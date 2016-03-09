/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.hub.data;

import com.whizzosoftware.hobson.api.telemetry.DataStream;
import com.whizzosoftware.hobson.api.telemetry.TelemetryInterval;
import com.whizzosoftware.hobson.api.telemetry.TelemetryManager;
import com.whizzosoftware.hobson.api.telemetry.TemporalValueSet;
import com.whizzosoftware.hobson.api.variable.VariableContext;
import com.whizzosoftware.hobson.hub.data.db.DataStreamDB;
import com.whizzosoftware.hobson.hub.data.db.TelemetryFileContext;
import com.whizzosoftware.hobson.hub.data.store.DataStreamStore;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * A local (hub-based) implementation of TelemetryManager. It currently uses MapDB to store data stream meta data
 * and RRD4J to store data stream data.
 *
 * @author Dan Noguerol
 */
public class LocalTelemetryManager implements TelemetryManager, TelemetryFileContext {
    private FileProvider fileProvider;
    private DataStreamStore store;
    private DataStreamDB db;

    public LocalTelemetryManager(FileProvider fileProvider, DataStreamStore store, DataStreamDB db) {
        this.fileProvider = fileProvider;
        this.store = store;
        this.db = db;
    }

    @Override
    public boolean isStub() {
        return false;
    }

    @Override
    public String createDataStream(String userId, String name, Collection<VariableContext> data) {
        return store.saveDataStream(userId, UUID.randomUUID().toString(), name, data).getId();
    }

    @Override
    public Collection<DataStream> getDataStreams(String userId) {
        return store.getDataStreams(userId);
    }

    @Override
    public DataStream getDataStream(String userId, String dataStreamId) {
        return store.getDataStream(userId, dataStreamId);
    }

    @Override
    public Set<VariableContext> getMonitoredVariables(String userId) {
        // TODO
        return null;
    }

    @Override
    public void addData(String userId, String dataStreamId, long now, Map<VariableContext, Object> data) {
        db.addData(this, userId, dataStreamId, now, data);
    }

    @Override
    public List<TemporalValueSet> getData(String userId, String dataStreamId, long endTime, TelemetryInterval interval) {
        return db.getData(this, userId, dataStreamId, endTime, interval);
    }

    @Override
    public File getFile(String userId, String dataStreamId) throws IOException {
        return fileProvider.getTelemetryDataFile(userId, dataStreamId);
    }

    @Override
    public Collection<VariableContext> getVariables(String userId, String dataStreamId) {
        DataStream ds = store.getDataStream(userId, dataStreamId);
        return ds.getVariables();
    }
}
