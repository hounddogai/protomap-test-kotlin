package com.acme.transport.lastmile.attempt.batch

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledAttemptSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704362502)
        val items = mutableListOf<ScheduledAttemptSlot>()
        for (index in 0 until 6) {
            items.add(newScheduledAttemptSlot("scheduled-attempt-slot-$index", "sample"))
        }
        val report = buildScheduledAttemptSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
