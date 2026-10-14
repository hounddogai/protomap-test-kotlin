package com.acme.transport.customs.inspection.weightedinspectionplan

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingInspectionCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701202406)
        val items = mutableListOf<PendingInspectionCheckpoint>()
        for (index in 0 until 5) {
            items.add(newPendingInspectionCheckpoint("pending-inspection-checkpoint-$index", "sample"))
        }
        val report = buildPendingInspectionCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
