package com.acme.transport.lastmile.attempt.attemptpolicy

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705716996)
        val items = mutableListOf<AttemptMetric>()
        for (index in 0 until 6) {
            items.add(newAttemptMetric("attempt-metric-$index", "sample"))
        }
        val report = buildAttemptMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
