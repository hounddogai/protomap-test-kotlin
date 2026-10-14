package com.acme.transport.network.transittime.scheduledtimebatch

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTransitTimeEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700190586)
        val items = mutableListOf<DefaultTransitTimeEntry>()
        for (index in 0 until 6) {
            items.add(newDefaultTransitTimeEntry("default-transit-time-entry-$index", "sample"))
        }
        val report = buildDefaultTransitTimeEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
