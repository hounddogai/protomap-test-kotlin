package com.acme.transport.emissions.idling.idlingwindow

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultIdlingMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709886715)
        val items = mutableListOf<DefaultIdlingMetric>()
        for (index in 0 until 3) {
            items.add(newDefaultIdlingMetric("default-idling-metric-$index", "sample"))
        }
        val report = buildDefaultIdlingMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
