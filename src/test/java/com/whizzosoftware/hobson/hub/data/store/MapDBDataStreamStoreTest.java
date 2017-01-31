package com.whizzosoftware.hobson.hub.data.store;

import com.whizzosoftware.hobson.api.data.DataStreamField;
import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.data.DataStream;
import com.whizzosoftware.hobson.api.hub.HubContext;
import com.whizzosoftware.hobson.api.variable.DeviceVariableContext;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MapDBDataStreamStoreTest {
    @Test
    public void testSaveAndRestoreDataStream() throws Exception {
        File f = File.createTempFile("test", ".mapdb");
        f.deleteOnExit();

        MapDBDataStreamStore s = new MapDBDataStreamStore(f);

        // get a list of data streams
        assertEquals(0, s.getDataStreams(HubContext.createLocal()).size());

        // create a new data stream
        List<DataStreamField> data = new ArrayList<>();
        data.add(new DataStreamField("id1", "field1", DeviceVariableContext.create(DeviceContext.createLocal("plugin1", "device1"), "on")));
        data.add(new DataStreamField("id2", "field2", DeviceVariableContext.create(DeviceContext.createLocal("plugin1", "device2"), "outTempF")));
        data.add(new DataStreamField("id3", "field3", DeviceVariableContext.create(DeviceContext.createLocal("plugin1", "device3"), "inTempF")));
        HashSet<String> tags = new HashSet<>();
        tags.add("tag1");
        tags.add("tag2");
        DataStream ds = s.saveDataStream(HubContext.createLocal(), "foo", "My Data Stream", data, tags);
        assertNotNull(ds);
        assertEquals("foo", ds.getId());
        assertEquals("My Data Stream", ds.getName());
        assertNotNull(ds.getFields());
        assertEquals(3, ds.getFields().size());

        // re-open the database and try to restore the data stream
        s.close();
        s = new MapDBDataStreamStore(f);
        ds = s.getDataStream(HubContext.createLocal(), "foo");
        assertNotNull(ds);
        assertEquals("foo", ds.getId());
        assertEquals("My Data Stream", ds.getName());
        assertNotNull(ds.getFields());
        assertEquals(3, ds.getFields().size());
        assertNotNull(ds.getTags());
        assertEquals(2, ds.getTags().size());
        assertTrue(ds.getTags().contains("tag1"));
        assertTrue(ds.getTags().contains("tag2"));

        // get a list of data streams
        assertEquals(1, s.getDataStreams(HubContext.createLocal()).size());
    }
}
