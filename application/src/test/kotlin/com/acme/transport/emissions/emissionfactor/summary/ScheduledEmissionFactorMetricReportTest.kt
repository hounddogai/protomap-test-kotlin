package com.acme.transport.emissions.emissionfactor.summary

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledEmissionFactorMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701325869)
        val items = mutableListOf<ScheduledEmissionFactorMetric>()
        for (index in 0 until 4) {
            items.add(newScheduledEmissionFactorMetric("scheduled-emission-factor-metric-$index", "sample"))
        }
        val report = buildScheduledEmissionFactorMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
