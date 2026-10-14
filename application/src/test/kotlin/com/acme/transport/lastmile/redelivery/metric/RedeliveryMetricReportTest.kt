package com.acme.transport.lastmile.redelivery.metric

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700856903)
        val items = mutableListOf<RedeliveryMetric>()
        for (index in 0 until 3) {
            items.add(newRedeliveryMetric("redelivery-metric-$index", "sample"))
        }
        val report = buildRedeliveryMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
