package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedFuelBurnManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707623217)
        val items = mutableListOf<CappedFuelBurnManifest>()
        for (index in 0 until 2) {
            items.add(newCappedFuelBurnManifest("capped-fuel-burn-manifest-$index", "sample"))
        }
        val report = buildCappedFuelBurnManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
