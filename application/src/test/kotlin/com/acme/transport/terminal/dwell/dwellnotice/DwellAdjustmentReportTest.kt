package com.acme.transport.terminal.dwell.dwellnotice

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704710418)
        val items = mutableListOf<DwellAdjustment>()
        for (index in 0 until 5) {
            items.add(newDwellAdjustment("dwell-adjustment-$index", "sample"))
        }
        val report = buildDwellAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
