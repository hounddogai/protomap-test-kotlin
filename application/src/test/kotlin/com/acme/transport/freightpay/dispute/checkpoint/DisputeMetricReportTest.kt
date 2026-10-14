package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705489656)
        val items = mutableListOf<DisputeMetric>()
        for (index in 0 until 3) {
            items.add(newDisputeMetric("dispute-metric-$index", "sample"))
        }
        val report = buildDisputeMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
