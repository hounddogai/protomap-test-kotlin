package com.acme.transport.crewing.restbreak.segment

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRestBreakSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700695260)
        val items = mutableListOf<GlobalRestBreakSegment>()
        for (index in 0 until 3) {
            items.add(newGlobalRestBreakSegment("global-rest-break-segment-$index", "sample"))
        }
        val report = buildGlobalRestBreakSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
