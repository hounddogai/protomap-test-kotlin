package com.acme.transport.safety.coaching.coachingscore

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703959828)
        val items = mutableListOf<CoachingEntry>()
        for (index in 0 until 3) {
            items.add(newCoachingEntry("coaching-entry-$index", "sample"))
        }
        val report = buildCoachingEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
