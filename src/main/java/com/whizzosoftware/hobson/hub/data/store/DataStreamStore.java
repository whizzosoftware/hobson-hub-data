/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.hub.data.store;

import com.whizzosoftware.hobson.api.telemetry.DataStream;
import com.whizzosoftware.hobson.api.variable.VariableContext;

import java.util.Collection;

/**
 * An interface for classes that can store/retrieve data stream meta data.
 *
 * @author Dan Noguerol
 */
public interface DataStreamStore {
    /**
     * Saves data stream information.
     *
     * @param userId the user ID for the data stream
     * @param dataStreamId the data stream ID
     * @param name the name of the data stream
     * @param data the list of variables associated with the data stream
     *
     * @return
     */
    DataStream saveDataStream(String userId, String dataStreamId, String name, Collection<VariableContext> data);

    /**
     * Retrieves a list of all available data streams.
     *
     * @param userId the user ID for the data stream
     *
     * @return
     */
    Collection<DataStream> getDataStreams(String userId);

    /**
     * Retrieves the details of a specific data stream.
     *
     * @param userId the user ID for the data stream
     * @param dataStreamId the data stream ID
     *
     * @return
     */
    DataStream getDataStream(String userId, String dataStreamId);
}
