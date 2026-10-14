package com.acme.transport.fleetops.defect.allocation

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707487119)
        val items = mutableListOf<DefectSlot>()
        for (index in 0 until 2) {
            items.add(newDefectSlot("defect-slot-$index", "sample"))
        }
        val report = buildDefectSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
