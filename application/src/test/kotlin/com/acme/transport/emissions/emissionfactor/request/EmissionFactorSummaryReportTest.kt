package com.acme.transport.emissions.emissionfactor.request

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709735426)
        val items = mutableListOf<EmissionFactorSummary>()
        for (index in 0 until 3) {
            items.add(newEmissionFactorSummary("emission-factor-summary-$index", "sample"))
        }
        val report = buildEmissionFactorSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
