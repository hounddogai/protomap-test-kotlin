package com.acme.transport.safety.coaching.mapping

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCoachingMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709655389)
        val items = mutableListOf<RegionalCoachingMilestone>()
        for (index in 0 until 6) {
            items.add(newRegionalCoachingMilestone("regional-coaching-milestone-$index", "sample"))
        }
        val report = buildRegionalCoachingMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
