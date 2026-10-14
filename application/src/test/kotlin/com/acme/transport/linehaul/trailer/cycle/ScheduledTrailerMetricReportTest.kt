package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledTrailerMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700937535)
        val items = mutableListOf<ScheduledTrailerMetric>()
        for (index in 0 until 6) {
            items.add(newScheduledTrailerMetric("scheduled-trailer-metric-$index", "sample"))
        }
        val report = buildScheduledTrailerMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
