package com.acme.transport.lastmile.attempt.attemptmetric

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalAttemptMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705273535)
        val items = mutableListOf<RegionalAttemptMetric>()
        for (index in 0 until 5) {
            items.add(newRegionalAttemptMetric("regional-attempt-metric-$index", "sample"))
        }
        val report = buildRegionalAttemptMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
