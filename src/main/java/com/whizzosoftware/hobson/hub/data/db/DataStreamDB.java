/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.hub.data.db;

import com.whizzosoftware.hobson.api.data.DataStreamInterval;
import com.whizzosoftware.hobson.api.data.DataStreamValueSet;

import java.util.List;
import java.util.Map;

/**
 * An interface implemented by classes that can store and retrieve data stream data.
 *
 * @author Dan Noguerol
 */
public interface DataStreamDB {
    /**
     * Adds new data to a data stream.
     * @param context the context
     * @param dataStreamId the data stream ID
     * @param time the time at which the data occurred
     * @param data a map of fieldId to value
     */
    void addData(DataStreamFileContext context, String dataStreamId, long time, Map<String, Object> data);

    /**
     * Retrieves data stream data.
     *
     * @param context the context
     * @param dataStreamId the data stream ID
     * @param endTime the end time for the desired data retrieval interval
     * @param interval the data interval length
     *
     * @return a List of TemporalValueSet instances (empty if no data was found for the requested interval)
     */
    List<DataStreamValueSet> getData(DataStreamFileContext context, String dataStreamId, long endTime, DataStreamInterval interval);
}
