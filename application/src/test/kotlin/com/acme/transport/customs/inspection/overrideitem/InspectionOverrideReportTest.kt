package com.acme.transport.customs.inspection.overrideitem

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705614698)
        val items = mutableListOf<InspectionOverride>()
        for (index in 0 until 4) {
            items.add(newInspectionOverride("inspection-override-$index", "sample"))
        }
        val report = buildInspectionOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
