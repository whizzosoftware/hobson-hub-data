package com.whizzosoftware.hobson.hub.data.db;

import com.whizzosoftware.hobson.api.data.DataStreamField;
import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.data.DataStreamInterval;
import com.whizzosoftware.hobson.api.data.DataStreamValueSet;
import com.whizzosoftware.hobson.api.variable.VariableContext;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RRD4JDataStreamDBTest {
    @Test
    public void testAddData() throws Exception {
        RRD4JDataStreamDB db = new RRD4JDataStreamDB();
        final VariableContext vctx1 = VariableContext.create(DeviceContext.createLocal("plugin1", "device1"), "outTempF");
        final VariableContext vctx2 = VariableContext.create(DeviceContext.createLocal("plugin1", "device2"), "inTempF");

        final File file = File.createTempFile("ds-", ".rrdb");
        file.deleteOnExit();
        file.delete();

        DataStreamFileContext ctx = new DataStreamFileContext() {
            @Override
            public File getFile(String userId, String dataStreamId) throws IOException {
                return file;
            }

            @Override
            public Collection<DataStreamField> getFields(String userId, String dataStreamId) {
                List<DataStreamField> list = new ArrayList<>();
                list.add(new DataStreamField("foo1", "field1", vctx1));
                list.add(new DataStreamField("foo2", "field2", vctx2));
                return list;
            }
        };

        long now = System.currentTimeMillis() + 1000;

        Map<String,Object> data = new HashMap<>();
        data.put("foo1", 35.0);
        data.put("foo2", 72.0);

        db.addData(ctx, "foo", now, data);
        // adding second data point forces first to be recorded
        db.addData(ctx, "foo", now + 1000 * 60 * 5, data);

        List<DataStreamValueSet> vals = db.getData(ctx, "foo", now + 1000 * 60 * 60 * 2, DataStreamInterval.HOURS_4);

        assertEquals(1, vals.size());
        DataStreamValueSet tvs = vals.get(0);
        assertNotNull(tvs.getTime());
        assertEquals(35.0, tvs.getValues().get("foo1"));
        assertEquals(72.0, tvs.getValues().get("foo2"));
    }
}
