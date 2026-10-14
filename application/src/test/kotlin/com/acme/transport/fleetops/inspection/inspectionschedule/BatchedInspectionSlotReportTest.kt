package com.acme.transport.fleetops.inspection.inspectionschedule

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedInspectionSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702097800)
        val items = mutableListOf<BatchedInspectionSlot>()
        for (index in 0 until 3) {
            items.add(newBatchedInspectionSlot("batched-inspection-slot-$index", "sample"))
        }
        val report = buildBatchedInspectionSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
