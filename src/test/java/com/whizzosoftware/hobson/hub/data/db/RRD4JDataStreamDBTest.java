package com.whizzosoftware.hobson.hub.data.db;

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.telemetry.TelemetryInterval;
import com.whizzosoftware.hobson.api.telemetry.TemporalValueSet;
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

        TelemetryFileContext ctx = new TelemetryFileContext() {
            @Override
            public File getFile(String userId, String dataStreamId) throws IOException {
                return file;
            }

            @Override
            public Collection<VariableContext> getVariables(String userId, String dataStreamId) {
                List<VariableContext> list = new ArrayList<>();
                list.add(vctx1);
                list.add(vctx2);
                return list;
            }
        };

        long now = System.currentTimeMillis() + 1000;

        Map<VariableContext,Object> data = new HashMap<>();
        data.put(vctx1, 35.0);
        data.put(vctx2, 72.0);

        db.addData(ctx, "local", "foo", now, data);
        // adding second data point forces first to be recorded
        db.addData(ctx, "local", "foo", now + 1000 * 60 * 5, data);

        List<TemporalValueSet> vals = db.getData(ctx, "local", "foo", now + 1000 * 60 * 60 * 2, TelemetryInterval.HOURS_4);

        assertEquals(1, vals.size());
        TemporalValueSet tvs = vals.get(0);
        assertNotNull(tvs.getTime());
        assertEquals(35.0, tvs.getValues().get("outTempF"));
        assertEquals(72.0, tvs.getValues().get("inTempF"));
    }
}
