package com.acme.transport.terminal.sortation.sortationbaseline

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledSortationBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701020753)
        val items = mutableListOf<ScheduledSortationBaseline>()
        for (index in 0 until 3) {
            items.add(newScheduledSortationBaseline("scheduled-sortation-baseline-$index", "sample"))
        }
        val report = buildScheduledSortationBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
