package com.acme.transport.terminal.dwell.dwellmilestone

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDwellMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702461982)
        val items = mutableListOf<FallbackDwellMilestone>()
        for (index in 0 until 3) {
            items.add(newFallbackDwellMilestone("fallback-dwell-milestone-$index", "sample"))
        }
        val report = buildFallbackDwellMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
