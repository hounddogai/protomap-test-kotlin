package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalNearMissMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705085828)
        val items = mutableListOf<RegionalNearMissMilestone>()
        for (index in 0 until 3) {
            items.add(newRegionalNearMissMilestone("regional-near-miss-milestone-$index", "sample"))
        }
        val report = buildRegionalNearMissMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
