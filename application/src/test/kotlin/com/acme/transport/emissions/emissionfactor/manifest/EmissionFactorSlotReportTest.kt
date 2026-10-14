package com.acme.transport.emissions.emissionfactor.manifest

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705073021)
        val items = mutableListOf<EmissionFactorSlot>()
        for (index in 0 until 5) {
            items.add(newEmissionFactorSlot("emission-factor-slot-$index", "sample"))
        }
        val report = buildEmissionFactorSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
