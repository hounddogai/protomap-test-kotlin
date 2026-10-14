package com.acme.transport.lastmile.attempt.defaultattemptpolicy

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAttemptSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702414873)
        val items = mutableListOf<CappedAttemptSlot>()
        for (index in 0 until 3) {
            items.add(newCappedAttemptSlot("capped-attempt-slot-$index", "sample"))
        }
        val report = buildCappedAttemptSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
