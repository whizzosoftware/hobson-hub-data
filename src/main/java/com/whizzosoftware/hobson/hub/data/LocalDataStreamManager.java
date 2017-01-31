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

import com.whizzosoftware.hobson.api.HobsonNotFoundException;
import com.whizzosoftware.hobson.api.data.*;
import com.whizzosoftware.hobson.api.hub.HubContext;
import com.whizzosoftware.hobson.hub.data.db.DataStreamDB;
import com.whizzosoftware.hobson.hub.data.db.DataStreamFileContext;
import com.whizzosoftware.hobson.hub.data.store.DataStreamStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * A local (hub-based) implementation of DataStreamManager. It currently uses MapDB to store data stream meta data
 * and RRD4J to store data stream data.
 *
 * @author Dan Noguerol
 */
public class LocalDataStreamManager implements DataStreamManager, DataStreamFileContext {
    private static final Logger logger = LoggerFactory.getLogger(LocalDataStreamManager.class);

    private FileProvider fileProvider;
    private DataStreamStore store;
    private DataStreamDB db;

    LocalDataStreamManager(FileProvider fileProvider, DataStreamStore store, DataStreamDB db) {
        this.fileProvider = fileProvider;
        this.store = store;
        this.db = db;
    }

    @Override
    public boolean isStub() {
        return false;
    }

    @Override
    public String createDataStream(HubContext ctx, String name, Collection<DataStreamField> fields, Set<String> tags) {
        return store.saveDataStream(ctx, UUID.randomUUID().toString(), name, fields, tags).getId();
    }

    @Override
    public void deleteDataStream(HubContext ctx, String dataStreamId) {
        store.deleteDataStream(ctx, dataStreamId);
        File f = fileProvider.getDataStreamDataFile(ctx, dataStreamId);
        if (f.exists()) {
            if (!f.delete()) {
                logger.error("Unable to delete data stream data file");
            }
        }
    }

    @Override
    public Collection<DataStream> getDataStreams(HubContext ctx) {
        return store.getDataStreams(ctx);
    }

    @Override
    public DataStream getDataStream(HubContext ctx, String dataStreamId) {
        return store.getDataStream(ctx, dataStreamId);
    }

    @Override
    public void addData(HubContext ctx, String dataStreamId, long now, Map<String, Object> data) {
        db.addData(this, dataStreamId, now, data);
    }

    @Override
    public List<DataStreamValueSet> getData(HubContext ctx, String dataStreamId, long endTime, DataStreamInterval interval) {
        DataStream ds = store.getDataStream(ctx, dataStreamId);
        if (ds != null) {
            return db.getData(this, dataStreamId, endTime, interval);
        } else {
            throw new HobsonNotFoundException("Data stream does not exist");
        }
    }

    @Override
    public File getFile(HubContext ctx, String dataStreamId) throws IOException {
        return fileProvider.getDataStreamDataFile(ctx, dataStreamId);
    }

    @Override
    public Collection<DataStreamField> getFields(HubContext ctx, String dataStreamId) {
        DataStream ds = store.getDataStream(ctx, dataStreamId);
        return ds.getFields();
    }
}
