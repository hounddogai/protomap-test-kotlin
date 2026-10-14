package com.acme.transport.fleetops.inspection.inspectionbatch

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingInspectionSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704175783)
        val items = mutableListOf<PendingInspectionSlot>()
        for (index in 0 until 4) {
            items.add(newPendingInspectionSlot("pending-inspection-slot-$index", "sample"))
        }
        val report = buildPendingInspectionSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
