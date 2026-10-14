package com.acme.transport.emissions.emissionfactor.agreement

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalEmissionFactorQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709232293)
        val items = mutableListOf<SeasonalEmissionFactorQuota>()
        for (index in 0 until 6) {
            items.add(newSeasonalEmissionFactorQuota("seasonal-emission-factor-quota-$index", "sample"))
        }
        val report = buildSeasonalEmissionFactorQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
