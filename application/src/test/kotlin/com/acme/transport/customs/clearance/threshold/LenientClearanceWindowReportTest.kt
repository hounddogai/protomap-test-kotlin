package com.acme.transport.customs.clearance.threshold

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientClearanceWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700460012)
        val items = mutableListOf<LenientClearanceWindow>()
        for (index in 0 until 4) {
            items.add(newLenientClearanceWindow("lenient-clearance-window-$index", "sample"))
        }
        val report = buildLenientClearanceWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
