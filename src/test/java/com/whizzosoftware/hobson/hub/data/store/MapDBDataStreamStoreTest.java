package com.whizzosoftware.hobson.hub.data.store;

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.telemetry.DataStream;
import com.whizzosoftware.hobson.api.variable.VariableContext;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MapDBDataStreamStoreTest {
    @Test
    public void testSaveAndRestoreDataStream() throws Exception {
        File f = File.createTempFile("test", ".mapdb");
        f.deleteOnExit();

        MapDBDataStreamStore s = new MapDBDataStreamStore(f);

        // get a list of data streams
        assertEquals(0, s.getDataStreams("local").size());

        // create a new data stream
        List<VariableContext> data = new ArrayList<>();
        data.add(VariableContext.create(DeviceContext.createLocal("plugin1", "device1"), "on"));
        data.add(VariableContext.create(DeviceContext.createLocal("plugin1", "device2"), "outTempF"));
        data.add(VariableContext.create(DeviceContext.createLocal("plugin1", "device3"), "inTempF"));
        DataStream ds = s.saveDataStream("local", "foo", "My Data Stream", data);
        assertNotNull(ds);
        assertEquals("foo", ds.getId());
        assertEquals("My Data Stream", ds.getName());
        assertNotNull(ds.getVariables());
        assertEquals(3, ds.getVariables().size());

        // re-open the database and try to restore the data stream
        s.close();
        s = new MapDBDataStreamStore(f);
        ds = s.getDataStream("local", "foo");
        assertNotNull(ds);
        assertEquals("foo", ds.getId());
        assertEquals("My Data Stream", ds.getName());
        assertNotNull(ds.getVariables());
        assertEquals(3, ds.getVariables().size());

        // get a list of data streams
        assertEquals(1, s.getDataStreams("local").size());
    }
}
