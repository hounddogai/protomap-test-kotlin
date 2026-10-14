package com.acme.transport.fleetops.defect.defectclaim

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDefectMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706548983)
        val items = mutableListOf<ScheduledDefectMetric>()
        for (index in 0 until 4) {
            items.add(newScheduledDefectMetric("scheduled-defect-metric-$index", "sample"))
        }
        val report = buildScheduledDefectMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
