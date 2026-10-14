package com.acme.transport.emissions.fuelburn.milestone

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyFuelBurnDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701119289)
        val items = mutableListOf<LegacyFuelBurnDraft>()
        for (index in 0 until 2) {
            items.add(newLegacyFuelBurnDraft("legacy-fuel-burn-draft-$index", "sample"))
        }
        val report = buildLegacyFuelBurnDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
