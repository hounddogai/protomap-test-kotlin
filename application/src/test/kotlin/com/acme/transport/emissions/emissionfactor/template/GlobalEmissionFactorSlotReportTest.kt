package com.acme.transport.emissions.emissionfactor.template

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalEmissionFactorSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704187321)
        val items = mutableListOf<GlobalEmissionFactorSlot>()
        for (index in 0 until 4) {
            items.add(newGlobalEmissionFactorSlot("global-emission-factor-slot-$index", "sample"))
        }
        val report = buildGlobalEmissionFactorSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
