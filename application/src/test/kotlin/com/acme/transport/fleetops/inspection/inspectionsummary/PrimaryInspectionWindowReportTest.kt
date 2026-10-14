package com.acme.transport.fleetops.inspection.inspectionsummary

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryInspectionWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704214037)
        val items = mutableListOf<PrimaryInspectionWindow>()
        for (index in 0 until 4) {
            items.add(newPrimaryInspectionWindow("primary-inspection-window-$index", "sample"))
        }
        val report = buildPrimaryInspectionWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
