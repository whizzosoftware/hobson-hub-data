/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.hub.data;

import java.io.File;

/**
 * An interface for classes that can provide data stream data files.
 *
 * @author Dan Noguerol
 */
public interface FileProvider {
    /**
     * Returns a data stream file.
     *
     * @param userId the user ID associated with the file
     * @param dataStreamId the data stream ID associated with the file
     *
     * @return a File instance
     */
    File getDataStreamDataFile(String userId, String dataStreamId);
}
