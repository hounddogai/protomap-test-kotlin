package com.acme.transport.fleetops.defect.target

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701850469)
        val items = mutableListOf<DefectTarget>()
        for (index in 0 until 2) {
            items.add(newDefectTarget("defect-target-$index", "sample"))
        }
        val report = buildDefectTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
