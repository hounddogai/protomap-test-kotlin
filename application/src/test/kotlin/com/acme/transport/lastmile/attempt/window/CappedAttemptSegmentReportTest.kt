package com.acme.transport.lastmile.attempt.window

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAttemptSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700763463)
        val items = mutableListOf<CappedAttemptSegment>()
        for (index in 0 until 4) {
            items.add(newCappedAttemptSegment("capped-attempt-segment-$index", "sample"))
        }
        val report = buildCappedAttemptSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
