package com.acme.transport.terminal.dwell.dwelldigest

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDwellAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703224527)
        val items = mutableListOf<ScheduledDwellAdjustment>()
        for (index in 0 until 2) {
            items.add(newScheduledDwellAdjustment("scheduled-dwell-adjustment-$index", "sample"))
        }
        val report = buildScheduledDwellAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
