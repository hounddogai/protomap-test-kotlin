package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703417619)
        val items = mutableListOf<EmissionFactorClaim>()
        for (index in 0 until 2) {
            items.add(newEmissionFactorClaim("emission-factor-claim-$index", "sample"))
        }
        val report = buildEmissionFactorClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
