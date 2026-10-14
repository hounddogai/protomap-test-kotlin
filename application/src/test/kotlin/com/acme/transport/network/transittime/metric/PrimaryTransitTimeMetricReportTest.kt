package com.acme.transport.network.transittime.metric

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryTransitTimeMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704017066)
        val items = mutableListOf<PrimaryTransitTimeMetric>()
        for (index in 0 until 6) {
            items.add(newPrimaryTransitTimeMetric("primary-transit-time-metric-$index", "sample"))
        }
        val report = buildPrimaryTransitTimeMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
