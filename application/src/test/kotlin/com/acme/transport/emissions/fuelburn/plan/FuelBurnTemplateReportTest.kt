package com.acme.transport.emissions.fuelburn.plan

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703405526)
        val items = mutableListOf<FuelBurnTemplate>()
        for (index in 0 until 5) {
            items.add(newFuelBurnTemplate("fuel-burn-template-$index", "sample"))
        }
        val report = buildFuelBurnTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
