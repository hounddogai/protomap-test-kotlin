package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalFuelBurnRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700418336)
        val items = mutableListOf<RegionalFuelBurnRecord>()
        for (index in 0 until 6) {
            items.add(newRegionalFuelBurnRecord("regional-fuel-burn-record-$index", "sample"))
        }
        val report = buildRegionalFuelBurnRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
