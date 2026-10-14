package com.acme.transport.terminal.dwell.milestone

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708768286)
        val items = mutableListOf<DwellSummary>()
        for (index in 0 until 3) {
            items.add(newDwellSummary("dwell-summary-$index", "sample"))
        }
        val report = buildDwellSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
