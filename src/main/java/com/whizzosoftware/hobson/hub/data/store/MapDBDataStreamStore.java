/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.hub.data.store;

import com.whizzosoftware.hobson.api.persist.CollectionPersister;
import com.whizzosoftware.hobson.api.persist.ContextPathIdProvider;
import com.whizzosoftware.hobson.api.persist.IdProvider;
import com.whizzosoftware.hobson.api.telemetry.DataStream;
import com.whizzosoftware.hobson.api.variable.VariableContext;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A MapDB implementation of DataStreamStore.
 *
 * @author Dan Noguerol
 */
public class MapDBDataStreamStore implements DataStreamStore {
    private static final Logger logger = LoggerFactory.getLogger(MapDBDataStreamStore.class);

    private DB db;
    private IdProvider idProvider = new ContextPathIdProvider();
    private CollectionPersister persister = new CollectionPersister(idProvider);

    public MapDBDataStreamStore(File file) {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            db = DBMaker.newFileDB(file).closeOnJvmShutdown().make();
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public void close() {
        db.close();
    }

    @Override
    public DataStream saveDataStream(String userId, String dataStreamId, String name, Collection<VariableContext> data) {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            logger.debug("Saving data stream: {}", dataStreamId);
            DataStream ds = new DataStream(userId, dataStreamId, name, data);
            persister.saveDataStream(new MapDBCollectionPersistenceContext(db), ds);
            return ds;
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    @Override
    public Collection<DataStream> getDataStreams(String userId) {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            List<DataStream> results = new ArrayList<>();
            MapDBCollectionPersistenceContext ctx = new MapDBCollectionPersistenceContext(db);
            for (Object o : ctx.getSet(idProvider.createDataStreamsId(userId))) {
                results.add(persister.restoreDataStream(ctx, userId, o.toString()));
            }
            return results;
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    @Override
    public DataStream getDataStream(String userId, String dataStreamId) {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            MapDBCollectionPersistenceContext ctx = new MapDBCollectionPersistenceContext(db);
            return persister.restoreDataStream(ctx, userId, dataStreamId);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }
}
