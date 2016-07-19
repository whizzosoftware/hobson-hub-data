/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.hub.data.db;

import com.whizzosoftware.hobson.api.data.DataStreamField;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * An interface for objects that can provide the context needed for data stream storage and retrieval.
 *
 * @author Dan Noguerol
 */
public interface TelemetryFileContext {
    /**
     * Returns a raw telemetry File.
     *
     * @param userId the user ID for the data stream
     * @param dataStreamId the data stream ID
     *
     * @return a File instance
     * @throws IOException
     */
    File getFile(String userId, String dataStreamId) throws IOException;

    /**
     * Returns the variables associated with a data stream.
     *
     * @param userId the user ID for the data stream
     * @param dataStreamId the data stream ID
     *
     * @return a Collection of DataStreamField instances
     */
    Collection<DataStreamField> getFields(String userId, String dataStreamId);
}
