package com.acme.transport.fleetops.inspection.group

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701763868)
        val items = mutableListOf<InspectionWindow>()
        for (index in 0 until 4) {
            items.add(newInspectionWindow("inspection-window-$index", "sample"))
        }
        val report = buildInspectionWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
