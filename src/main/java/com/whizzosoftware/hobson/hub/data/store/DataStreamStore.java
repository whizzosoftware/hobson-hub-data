/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.hub.data.store;

import com.whizzosoftware.hobson.api.data.DataStream;
import com.whizzosoftware.hobson.api.data.DataStreamField;
import com.whizzosoftware.hobson.api.hub.HubContext;

import java.util.Collection;
import java.util.Set;

/**
 * An interface for classes that can store/retrieve data stream meta data.
 *
 * @author Dan Noguerol
 */
public interface DataStreamStore {
    /**
     * Deletes an existing data stream.
     *
     * @param ctx the hub context
     * @param dataStreamId the data stream ID
     */
    void deleteDataStream(HubContext ctx, String dataStreamId);

    /**
     * Saves data stream information.
     *
     * @param ctx the hub context
     * @param dataStreamId the data stream ID
     * @param name the name of the data stream
     * @param fields the list of fields associated with the data stream
     * @param tags the list of tags associated with the data stream
     *
     * @return a DataStream instance
     */
    DataStream saveDataStream(HubContext ctx, String dataStreamId, String name, Collection<DataStreamField> fields, Set<String> tags);

    /**
     * Retrieves a list of all available data streams.
     *
     * @param ctx the hub context
     *
     * @return  a Collection of DataStream instances
     */
    Collection<DataStream> getDataStreams(HubContext ctx);

    /**
     * Retrieves the details of a specific data stream.
     *
     * @param ctx the hub context
     * @param dataStreamId the data stream ID
     *
     * @return a DataStream instance
     */
    DataStream getDataStream(HubContext ctx, String dataStreamId);
}
