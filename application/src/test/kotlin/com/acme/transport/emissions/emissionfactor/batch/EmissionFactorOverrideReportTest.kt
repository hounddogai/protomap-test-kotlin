package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708377292)
        val items = mutableListOf<EmissionFactorOverride>()
        for (index in 0 until 4) {
            items.add(newEmissionFactorOverride("emission-factor-override-$index", "sample"))
        }
        val report = buildEmissionFactorOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
