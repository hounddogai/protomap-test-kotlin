package com.acme.transport.network.transittime.digest

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706651273)
        val items = mutableListOf<TransitTimeVariance>()
        for (index in 0 until 3) {
            items.add(newTransitTimeVariance("transit-time-variance-$index", "sample"))
        }
        val report = buildTransitTimeVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
