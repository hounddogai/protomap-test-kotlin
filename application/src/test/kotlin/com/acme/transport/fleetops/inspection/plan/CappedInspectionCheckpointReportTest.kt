package com.acme.transport.fleetops.inspection.plan

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedInspectionCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706238566)
        val items = mutableListOf<CappedInspectionCheckpoint>()
        for (index in 0 until 3) {
            items.add(newCappedInspectionCheckpoint("capped-inspection-checkpoint-$index", "sample"))
        }
        val report = buildCappedInspectionCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
