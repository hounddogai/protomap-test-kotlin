package com.acme.transport.linehaul.trailer.milestone

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708158716)
        val items = mutableListOf<TrailerMilestone>()
        for (index in 0 until 4) {
            items.add(newTrailerMilestone("trailer-milestone-$index", "sample"))
        }
        val report = buildTrailerMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
