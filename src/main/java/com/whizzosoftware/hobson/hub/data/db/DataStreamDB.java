/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.hub.data.db;

import com.whizzosoftware.hobson.api.telemetry.TelemetryInterval;
import com.whizzosoftware.hobson.api.telemetry.TemporalValueSet;
import com.whizzosoftware.hobson.api.variable.VariableContext;

import java.util.List;
import java.util.Map;

/**
 * An interface implemented by classes that can store and retrieve data stream data (telemetry).
 *
 * @author Dan Noguerol
 */
public interface DataStreamDB {
    /**
     * Adds new data to a data stream.
     *
     * @param context the context
     * @param userId the user ID that owns the data stream
     * @param dataStreamId the data stream ID
     * @param time the time at which the data occurred
     * @param data the data values
     */
    void addData(TelemetryFileContext context, String userId, String dataStreamId, long time, Map<VariableContext, Object> data);

    /**
     * Retrieves data stream data.
     *
     * @param context the context
     * @param userId the user ID that owns the data stream
     * @param dataStreamId the data stream ID
     * @param endTime the end time for the desired data retrieval interval
     * @param interval the data interval length
     *
     * @return a List of TemporalValueSet instances (empty if no data was found for the requested interval)
     */
    List<TemporalValueSet> getData(TelemetryFileContext context, String userId, String dataStreamId, long endTime, TelemetryInterval interval);
}
