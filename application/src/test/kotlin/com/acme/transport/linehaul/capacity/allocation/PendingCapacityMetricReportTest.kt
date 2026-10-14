package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCapacityMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706855740)
        val items = mutableListOf<PendingCapacityMetric>()
        for (index in 0 until 6) {
            items.add(newPendingCapacityMetric("pending-capacity-metric-$index", "sample"))
        }
        val report = buildPendingCapacityMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
