/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.hub.data.db;

import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.data.DataStreamField;
import com.whizzosoftware.hobson.api.data.DataStreamInterval;
import com.whizzosoftware.hobson.api.data.DataStreamValueSet;
import com.whizzosoftware.hobson.api.hub.HubContext;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.*;
import static org.rrd4j.ConsolFun.AVERAGE;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * An RRD4J implementation of DataStreamDB.
 *
 * @author Dan Noguerol
 */
public class RRD4JDataStreamDB implements DataStreamDB {
    @Override
    synchronized public void addData(DataStreamFileContext provider, String dataStreamId, long now, Map<String, Object> data) {
        try {
            File file = getDataStreamFile(provider, HubContext.createLocal(), dataStreamId);
            RrdDb db = new RrdDb(file.getAbsolutePath(), false);
            long t = now / 1000;
            Sample sample = db.createSample(t);
            for (String fieldId : data.keySet()) {
                sample.setValue(fieldId, Double.parseDouble(data.get(fieldId).toString()));
            }
            sample.update();
            db.close();
        } catch (IOException e) {
            throw new HobsonRuntimeException("Error writing to data stream file", e);
        }
    }

    @Override
    synchronized public List<DataStreamValueSet> getData(DataStreamFileContext provider, String dataStreamId, long endTime, DataStreamInterval interval) {
        try {
            // TODO: this whole thing is pretty inefficient; refactor
            Map<Long,DataStreamValueSet> map = new TreeMap<>();
            File file = getDataStreamFile(provider, HubContext.createLocal(), dataStreamId);
            if (file.exists()) {
                RrdDb db = new RrdDb(file.getAbsolutePath(), true);
                try {
                    long startTime = calculateStartTime(endTime / 1000, interval);
                    FetchRequest fetch = db.createFetchRequest(ConsolFun.AVERAGE, startTime, endTime / 1000);
                    FetchData data = fetch.fetchData();
                    long[] timestamps = data.getTimestamps();
                    for (String dsName : data.getDsNames()) {
                        double[] values = data.getValues(dsName);
                        for (int i=0; i < values.length; i++) {
                            if (!Double.isNaN(values[i])) {
                                long ts = timestamps[i] * 1000;
                                DataStreamValueSet tvs = map.get(ts);
                                if (tvs == null) {
                                    tvs = new DataStreamValueSet(ts);
                                    map.put(ts, tvs);
                                }
                                tvs.addValue(dsName, values[i]);
                            }
                        }
                    }
                } finally {
                    try {
                        db.close();
                    } catch (IOException ignored) {}
                }
            }
            return new ArrayList<>(map.values());
        } catch (IOException e) {
            throw new HobsonRuntimeException("Error retrieving device data stream", e);
        }
    }

    private long calculateStartTime(long endTime, DataStreamInterval interval) {
        switch (interval) {
            case HOURS_1:
                return endTime - (60 * 60L);
            case HOURS_4:
                return endTime - (4 * 60 * 60L);
            case DAYS_7:
                return endTime - (7 * 24 * 60 * 60L);
            case DAYS_30:
                return endTime - (30 * 24 * 60 * 60L);
            default:
                return endTime - (24 * 60 * 60L);
        }
    }

    private File getDataStreamFile(DataStreamFileContext provider, HubContext ctx, String dataStreamId) throws IOException {
        File file = provider.getFile(ctx, dataStreamId);
        if (!file.exists()) {
            Collection<DataStreamField> list = provider.getFields(ctx, dataStreamId);
            if (list != null) {
                RrdDef rrdDef = new RrdDef(file.getAbsolutePath(), Util.getTimestamp() - 1, 300); // 5 minute step
                rrdDef.setVersion(2);
                for (DataStreamField dsf : list) {
                    rrdDef.addDatasource(dsf.getId(), DsType.GAUGE, 300, Double.NaN, Double.NaN);
                }
                rrdDef.addArchive(AVERAGE, 0.5, 1, 2016); // 2016 = 7 days
                RrdDb db = new RrdDb(rrdDef);
                db.close();
            } else {
                throw new HobsonRuntimeException("Unable to determine variables in data stream");
            }
        }
        return file;
    }
}
