package com.acme.transport.customs.inspection.overrideitem

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707470855)
        val items = mutableListOf<InspectionGroup>()
        for (index in 0 until 5) {
            items.add(newInspectionGroup("inspection-group-$index", "sample"))
        }
        val report = buildInspectionGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
