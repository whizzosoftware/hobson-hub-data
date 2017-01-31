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

import com.whizzosoftware.hobson.api.data.DataStreamField;
import com.whizzosoftware.hobson.api.hub.HubContext;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * An interface for objects that can provide the context needed for data stream storage and retrieval.
 *
 * @author Dan Noguerol
 */
public interface DataStreamFileContext {
    /**
     * Returns a raw data stream File.
     *
     * @param ctx the hub context
     * @param dataStreamId the data stream ID
     *
     * @return a File instance
     * @throws IOException on failure
     */
    File getFile(HubContext ctx, String dataStreamId) throws IOException;

    /**
     * Returns the variables associated with a data stream.
     *
     * @param ctx the hub context
     * @param dataStreamId the data stream ID
     *
     * @return a Collection of DataStreamField instances
     */
    Collection<DataStreamField> getFields(HubContext ctx, String dataStreamId);
}
