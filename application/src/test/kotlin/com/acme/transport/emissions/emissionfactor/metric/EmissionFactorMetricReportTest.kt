package com.acme.transport.emissions.emissionfactor.metric

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703895183)
        val items = mutableListOf<EmissionFactorMetric>()
        for (index in 0 until 5) {
            items.add(newEmissionFactorMetric("emission-factor-metric-$index", "sample"))
        }
        val report = buildEmissionFactorMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
