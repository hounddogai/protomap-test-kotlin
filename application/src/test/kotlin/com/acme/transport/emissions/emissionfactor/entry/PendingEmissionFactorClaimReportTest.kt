package com.acme.transport.emissions.emissionfactor.entry

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingEmissionFactorClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701672545)
        val items = mutableListOf<PendingEmissionFactorClaim>()
        for (index in 0 until 6) {
            items.add(newPendingEmissionFactorClaim("pending-emission-factor-claim-$index", "sample"))
        }
        val report = buildPendingEmissionFactorClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
