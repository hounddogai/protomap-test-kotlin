package com.acme.transport.network.transittime.draft

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledTransitTimePreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702428412)
        val items = mutableListOf<ScheduledTransitTimePreference>()
        for (index in 0 until 2) {
            items.add(newScheduledTransitTimePreference("scheduled-transit-time-preference-$index", "sample"))
        }
        val report = buildScheduledTransitTimePreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
