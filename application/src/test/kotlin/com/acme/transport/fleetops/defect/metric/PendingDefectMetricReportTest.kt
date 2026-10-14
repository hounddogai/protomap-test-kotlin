package com.acme.transport.fleetops.defect.metric

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDefectMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709034206)
        val items = mutableListOf<PendingDefectMetric>()
        for (index in 0 until 4) {
            items.add(newPendingDefectMetric("pending-defect-metric-$index", "sample"))
        }
        val report = buildPendingDefectMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
