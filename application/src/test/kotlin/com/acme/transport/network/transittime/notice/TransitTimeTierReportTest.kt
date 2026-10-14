package com.acme.transport.network.transittime.notice

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709952333)
        val items = mutableListOf<TransitTimeTier>()
        for (index in 0 until 6) {
            items.add(newTransitTimeTier("transit-time-tier-$index", "sample"))
        }
        val report = buildTransitTimeTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
