package com.acme.transport.safety.coaching.tier

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultCoachingMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708248913)
        val items = mutableListOf<DefaultCoachingMetric>()
        for (index in 0 until 6) {
            items.add(newDefaultCoachingMetric("default-coaching-metric-$index", "sample"))
        }
        val report = buildDefaultCoachingMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
