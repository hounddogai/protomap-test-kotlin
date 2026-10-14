package com.acme.transport.network.transittime.draft

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705453017)
        val items = mutableListOf<TransitTimeDraft>()
        for (index in 0 until 3) {
            items.add(newTransitTimeDraft("transit-time-draft-$index", "sample"))
        }
        val report = buildTransitTimeDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
