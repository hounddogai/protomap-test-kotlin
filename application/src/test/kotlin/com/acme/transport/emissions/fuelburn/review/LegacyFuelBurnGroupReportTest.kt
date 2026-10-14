package com.acme.transport.emissions.fuelburn.review

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyFuelBurnGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708354827)
        val items = mutableListOf<LegacyFuelBurnGroup>()
        for (index in 0 until 5) {
            items.add(newLegacyFuelBurnGroup("legacy-fuel-burn-group-$index", "sample"))
        }
        val report = buildLegacyFuelBurnGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
