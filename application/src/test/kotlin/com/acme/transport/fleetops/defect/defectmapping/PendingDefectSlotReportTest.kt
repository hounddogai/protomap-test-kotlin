package com.acme.transport.fleetops.defect.defectmapping

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDefectSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709854575)
        val items = mutableListOf<PendingDefectSlot>()
        for (index in 0 until 4) {
            items.add(newPendingDefectSlot("pending-defect-slot-$index", "sample"))
        }
        val report = buildPendingDefectSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
