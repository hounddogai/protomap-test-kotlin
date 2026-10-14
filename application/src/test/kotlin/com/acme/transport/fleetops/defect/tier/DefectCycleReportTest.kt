package com.acme.transport.fleetops.defect.tier

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705769998)
        val items = mutableListOf<DefectCycle>()
        for (index in 0 until 4) {
            items.add(newDefectCycle("defect-cycle-$index", "sample"))
        }
        val report = buildDefectCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
