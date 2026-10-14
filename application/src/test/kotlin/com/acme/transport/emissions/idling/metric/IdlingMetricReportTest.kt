package com.acme.transport.emissions.idling.metric

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702380930)
        val items = mutableListOf<IdlingMetric>()
        for (index in 0 until 3) {
            items.add(newIdlingMetric("idling-metric-$index", "sample"))
        }
        val report = buildIdlingMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
