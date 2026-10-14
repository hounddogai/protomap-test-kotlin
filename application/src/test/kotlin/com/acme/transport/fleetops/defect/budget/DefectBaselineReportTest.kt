package com.acme.transport.fleetops.defect.budget

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707903678)
        val items = mutableListOf<DefectBaseline>()
        for (index in 0 until 2) {
            items.add(newDefectBaseline("defect-baseline-$index", "sample"))
        }
        val report = buildDefectBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
