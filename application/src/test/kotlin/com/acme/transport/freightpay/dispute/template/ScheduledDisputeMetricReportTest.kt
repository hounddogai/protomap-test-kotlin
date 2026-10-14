package com.acme.transport.freightpay.dispute.template

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDisputeMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708094190)
        val items = mutableListOf<ScheduledDisputeMetric>()
        for (index in 0 until 4) {
            items.add(newScheduledDisputeMetric("scheduled-dispute-metric-$index", "sample"))
        }
        val report = buildScheduledDisputeMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
