package com.acme.transport.emissions.emissionfactor.preference

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703693704)
        val items = mutableListOf<EmissionFactorRollup>()
        for (index in 0 until 3) {
            items.add(newEmissionFactorRollup("emission-factor-rollup-$index", "sample"))
        }
        val report = buildEmissionFactorRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
