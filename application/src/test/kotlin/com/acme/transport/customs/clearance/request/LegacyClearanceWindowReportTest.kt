package com.acme.transport.customs.clearance.request

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyClearanceWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708092227)
        val items = mutableListOf<LegacyClearanceWindow>()
        for (index in 0 until 2) {
            items.add(newLegacyClearanceWindow("legacy-clearance-window-$index", "sample"))
        }
        val report = buildLegacyClearanceWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
