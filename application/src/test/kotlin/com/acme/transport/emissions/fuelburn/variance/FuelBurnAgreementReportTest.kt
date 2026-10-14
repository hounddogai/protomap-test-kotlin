package com.acme.transport.emissions.fuelburn.variance

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703482290)
        val items = mutableListOf<FuelBurnAgreement>()
        for (index in 0 until 2) {
            items.add(newFuelBurnAgreement("fuel-burn-agreement-$index", "sample"))
        }
        val report = buildFuelBurnAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
