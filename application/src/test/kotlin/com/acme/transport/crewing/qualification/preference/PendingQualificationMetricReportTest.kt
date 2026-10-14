package com.acme.transport.crewing.qualification.preference

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingQualificationMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703954742)
        val items = mutableListOf<PendingQualificationMetric>()
        for (index in 0 until 6) {
            items.add(newPendingQualificationMetric("pending-qualification-metric-$index", "sample"))
        }
        val report = buildPendingQualificationMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
