package com.acme.transport.safety.coaching.coachingmetric

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalCoachingMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705912001)
        val items = mutableListOf<GlobalCoachingMetric>()
        for (index in 0 until 2) {
            items.add(newGlobalCoachingMetric("global-coaching-metric-$index", "sample"))
        }
        val report = buildGlobalCoachingMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
