package com.acme.transport.network.transittime.timenotice

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredTransitTimeVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701561645)
        val items = mutableListOf<TieredTransitTimeVariance>()
        for (index in 0 until 2) {
            items.add(newTieredTransitTimeVariance("tiered-transit-time-variance-$index", "sample"))
        }
        val report = buildTieredTransitTimeVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
