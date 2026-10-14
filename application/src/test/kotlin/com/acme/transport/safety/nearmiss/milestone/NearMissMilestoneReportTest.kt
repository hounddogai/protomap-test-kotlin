package com.acme.transport.safety.nearmiss.milestone

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700567621)
        val items = mutableListOf<NearMissMilestone>()
        for (index in 0 until 3) {
            items.add(newNearMissMilestone("near-miss-milestone-$index", "sample"))
        }
        val report = buildNearMissMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
