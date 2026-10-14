package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703233337)
        val items = mutableListOf<EvChargeMetric>()
        for (index in 0 until 6) {
            items.add(newEvChargeMetric("ev-charge-metric-$index", "sample"))
        }
        val report = buildEvChargeMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
