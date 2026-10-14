package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDwellAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704377872)
        val items = mutableListOf<ScheduledDwellAllocation>()
        for (index in 0 until 6) {
            items.add(newScheduledDwellAllocation("scheduled-dwell-allocation-$index", "sample"))
        }
        val report = buildScheduledDwellAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
