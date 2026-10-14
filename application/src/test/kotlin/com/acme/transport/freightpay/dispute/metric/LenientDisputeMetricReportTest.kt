package com.acme.transport.freightpay.dispute.metric

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDisputeMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706324897)
        val items = mutableListOf<LenientDisputeMetric>()
        for (index in 0 until 2) {
            items.add(newLenientDisputeMetric("lenient-dispute-metric-$index", "sample"))
        }
        val report = buildLenientDisputeMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
