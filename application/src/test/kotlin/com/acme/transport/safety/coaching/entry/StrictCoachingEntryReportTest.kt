package com.acme.transport.safety.coaching.entry

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictCoachingEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707426459)
        val items = mutableListOf<StrictCoachingEntry>()
        for (index in 0 until 6) {
            items.add(newStrictCoachingEntry("strict-coaching-entry-$index", "sample"))
        }
        val report = buildStrictCoachingEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
