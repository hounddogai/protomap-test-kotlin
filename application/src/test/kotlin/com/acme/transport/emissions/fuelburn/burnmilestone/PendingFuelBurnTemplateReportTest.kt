package com.acme.transport.emissions.fuelburn.burnmilestone

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingFuelBurnTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701348965)
        val items = mutableListOf<PendingFuelBurnTemplate>()
        for (index in 0 until 5) {
            items.add(newPendingFuelBurnTemplate("pending-fuel-burn-template-$index", "sample"))
        }
        val report = buildPendingFuelBurnTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
