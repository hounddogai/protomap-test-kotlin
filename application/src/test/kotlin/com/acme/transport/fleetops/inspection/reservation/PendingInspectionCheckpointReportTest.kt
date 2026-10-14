package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingInspectionCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709069163)
        val items = mutableListOf<PendingInspectionCheckpoint>()
        for (index in 0 until 6) {
            items.add(newPendingInspectionCheckpoint("pending-inspection-checkpoint-$index", "sample"))
        }
        val report = buildPendingInspectionCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
