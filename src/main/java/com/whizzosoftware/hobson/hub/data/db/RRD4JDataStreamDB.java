/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.hub.data.db;

import com.whizzosoftware.hobson.api.HobsonRuntimeException;
import com.whizzosoftware.hobson.api.telemetry.TelemetryInterval;
import com.whizzosoftware.hobson.api.telemetry.TemporalValueSet;
import com.whizzosoftware.hobson.api.variable.VariableContext;
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
    synchronized public void addData(TelemetryFileContext provider, String userId, String dataStreamId, long now, Map<VariableContext, Object> data) {
        try {
            File file = getTelemetryFile(provider, userId, dataStreamId);
            RrdDb db = new RrdDb(file.getAbsolutePath(), false);
            long t = now / 1000;
            Sample sample = db.createSample(t);
            for (VariableContext vctx : data.keySet()) {
                sample.setValue(vctx.getName(), Double.parseDouble(data.get(vctx).toString()));
            }
            sample.update();
            db.close();
        } catch (IOException e) {
            throw new HobsonRuntimeException("Error writing to telemetry file", e);
        }
    }

    @Override
    synchronized public List<TemporalValueSet> getData(TelemetryFileContext provider, String userId, String dataStreamId, long endTime, TelemetryInterval interval) {
        try {
            // TODO: this whole thing is pretty inefficient; refactor
            Map<Long,TemporalValueSet> map = new TreeMap<>();
            File file = getTelemetryFile(provider, userId, dataStreamId);
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
                                TemporalValueSet tvs = map.get(ts);
                                if (tvs == null) {
                                    tvs = new TemporalValueSet(ts);
                                    map.put(ts, tvs);
                                }
                                tvs.addValue(dsName, values[i]);
                            }
                        }
                    }
                } finally {
                    db.close();
                }
            }
            return new ArrayList<>(map.values());
        } catch (IOException e) {
            throw new HobsonRuntimeException("Error retrieving device telemetry", e);
        }
    }

    protected long calculateStartTime(long endTime, TelemetryInterval interval) {
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

    protected File getTelemetryFile(TelemetryFileContext provider, String userId, String dataStreamId) throws IOException {
        File file = provider.getFile(userId, dataStreamId);
        if (!file.exists()) {
            Collection<VariableContext> list = provider.getVariables(userId, dataStreamId);
            if (list != null) {
                RrdDef rrdDef = new RrdDef(file.getAbsolutePath(), Util.getTimestamp() - 1, 300); // 5 minute step
                rrdDef.setVersion(2);
                for (VariableContext ctx : list) {
                    rrdDef.addDatasource(ctx.getName(), DsType.GAUGE, 300, Double.NaN, Double.NaN);
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
