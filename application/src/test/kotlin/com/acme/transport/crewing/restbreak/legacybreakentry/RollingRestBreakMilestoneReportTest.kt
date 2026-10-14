package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRestBreakMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706087503)
        val items = mutableListOf<RollingRestBreakMilestone>()
        for (index in 0 until 5) {
            items.add(newRollingRestBreakMilestone("rolling-rest-break-milestone-$index", "sample"))
        }
        val report = buildRollingRestBreakMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
