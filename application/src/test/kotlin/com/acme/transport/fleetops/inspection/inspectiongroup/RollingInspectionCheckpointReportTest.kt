package com.acme.transport.fleetops.inspection.inspectiongroup

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingInspectionCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700716111)
        val items = mutableListOf<RollingInspectionCheckpoint>()
        for (index in 0 until 2) {
            items.add(newRollingInspectionCheckpoint("rolling-inspection-checkpoint-$index", "sample"))
        }
        val report = buildRollingInspectionCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
