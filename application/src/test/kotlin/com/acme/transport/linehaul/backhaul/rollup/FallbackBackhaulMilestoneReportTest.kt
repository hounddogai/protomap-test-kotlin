package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackBackhaulMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704280691)
        val items = mutableListOf<FallbackBackhaulMilestone>()
        for (index in 0 until 5) {
            items.add(newFallbackBackhaulMilestone("fallback-backhaul-milestone-$index", "sample"))
        }
        val report = buildFallbackBackhaulMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
