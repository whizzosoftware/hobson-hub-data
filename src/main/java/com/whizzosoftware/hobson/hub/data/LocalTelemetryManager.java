/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.hub.data;

import com.whizzosoftware.hobson.api.HobsonNotFoundException;
import com.whizzosoftware.hobson.api.data.*;
import com.whizzosoftware.hobson.api.variable.VariableContext;
import com.whizzosoftware.hobson.hub.data.db.DataStreamDB;
import com.whizzosoftware.hobson.hub.data.db.TelemetryFileContext;
import com.whizzosoftware.hobson.hub.data.store.DataStreamStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(LocalTelemetryManager.class);

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
    public String createDataStream(String userId, String name, Collection<DataStreamField> fields, Set<String> tags) {
        return store.saveDataStream(UUID.randomUUID().toString(), name, fields, tags).getId();
    }

    @Override
    public void deleteDataStream(String userId, String dataStreamId) {
        store.deleteDataStream(dataStreamId);
        File f = fileProvider.getTelemetryDataFile(userId, dataStreamId);
        if (f.exists()) {
            if (!f.delete()) {
                logger.error("Unable to delete telemetry data file");
            }
        }
    }

    @Override
    public Collection<DataStream> getDataStreams(String userId) {
        return store.getDataStreams(userId);
    }

    @Override
    public DataStream getDataStream(String userId, String dataStreamId) {
        return store.getDataStream(dataStreamId);
    }

    @Override
    public Set<VariableContext> getMonitoredVariables(String userId) {
        // TODO
        return null;
    }

    @Override
    public void addData(String userId, String dataStreamId, long now, Map<String, Object> data) {
        db.addData(this, dataStreamId, now, data);
    }

    @Override
    public List<TemporalValueSet> getData(String userId, String dataStreamId, long endTime, TelemetryInterval interval) {
        DataStream ds = store.getDataStream(dataStreamId);
        if (ds != null) {
            return db.getData(this, dataStreamId, endTime, interval);
        } else {
            throw new HobsonNotFoundException("Data stream does not exist");
        }
    }

    @Override
    public File getFile(String userId, String dataStreamId) throws IOException {
        return fileProvider.getTelemetryDataFile(userId, dataStreamId);
    }

    @Override
    public Collection<DataStreamField> getFields(String userId, String dataStreamId) {
        DataStream ds = store.getDataStream(dataStreamId);
        return ds.getFields();
    }
}
