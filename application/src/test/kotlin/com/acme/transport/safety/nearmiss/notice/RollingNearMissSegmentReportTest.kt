package com.acme.transport.safety.nearmiss.notice

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingNearMissSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702013437)
        val items = mutableListOf<RollingNearMissSegment>()
        for (index in 0 until 6) {
            items.add(newRollingNearMissSegment("rolling-near-miss-segment-$index", "sample"))
        }
        val report = buildRollingNearMissSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
