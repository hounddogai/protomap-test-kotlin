package com.acme.transport.emissions.emissionfactor.claim

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedEmissionFactorThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700149552)
        val items = mutableListOf<WeightedEmissionFactorThreshold>()
        for (index in 0 until 2) {
            items.add(newWeightedEmissionFactorThreshold("weighted-emission-factor-threshold-$index", "sample"))
        }
        val report = buildWeightedEmissionFactorThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
