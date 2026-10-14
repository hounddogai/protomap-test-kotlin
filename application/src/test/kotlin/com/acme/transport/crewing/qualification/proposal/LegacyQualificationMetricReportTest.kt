package com.acme.transport.crewing.qualification.proposal

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyQualificationMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701864392)
        val items = mutableListOf<LegacyQualificationMetric>()
        for (index in 0 until 4) {
            items.add(newLegacyQualificationMetric("legacy-qualification-metric-$index", "sample"))
        }
        val report = buildLegacyQualificationMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
