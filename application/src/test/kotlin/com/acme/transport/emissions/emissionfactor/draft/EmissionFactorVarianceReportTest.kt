package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704042318)
        val items = mutableListOf<EmissionFactorVariance>()
        for (index in 0 until 3) {
            items.add(newEmissionFactorVariance("emission-factor-variance-$index", "sample"))
        }
        val report = buildEmissionFactorVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
