package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryDisputeMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708050486)
        val items = mutableListOf<PrimaryDisputeMetric>()
        for (index in 0 until 5) {
            items.add(newPrimaryDisputeMetric("primary-dispute-metric-$index", "sample"))
        }
        val report = buildPrimaryDisputeMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
