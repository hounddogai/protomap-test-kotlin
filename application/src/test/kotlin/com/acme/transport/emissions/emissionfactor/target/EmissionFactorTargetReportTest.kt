package com.acme.transport.emissions.emissionfactor.target

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705054611)
        val items = mutableListOf<EmissionFactorTarget>()
        for (index in 0 until 3) {
            items.add(newEmissionFactorTarget("emission-factor-target-$index", "sample"))
        }
        val report = buildEmissionFactorTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
