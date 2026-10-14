package com.acme.transport.emissions.emissionfactor.factorsegment

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707296559)
        val items = mutableListOf<EmissionFactorScore>()
        for (index in 0 until 6) {
            items.add(newEmissionFactorScore("emission-factor-score-$index", "sample"))
        }
        val report = buildEmissionFactorScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
