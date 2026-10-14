package com.acme.transport.emissions.fuelburn.review

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalFuelBurnDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703388552)
        val items = mutableListOf<GlobalFuelBurnDigest>()
        for (index in 0 until 2) {
            items.add(newGlobalFuelBurnDigest("global-fuel-burn-digest-$index", "sample"))
        }
        val report = buildGlobalFuelBurnDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
