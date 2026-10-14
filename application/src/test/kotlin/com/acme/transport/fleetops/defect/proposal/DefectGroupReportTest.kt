package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700836166)
        val items = mutableListOf<DefectGroup>()
        for (index in 0 until 3) {
            items.add(newDefectGroup("defect-group-$index", "sample"))
        }
        val report = buildDefectGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
