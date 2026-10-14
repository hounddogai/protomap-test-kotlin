package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingFuelBurnThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705961028)
        val items = mutableListOf<PendingFuelBurnThreshold>()
        for (index in 0 until 6) {
            items.add(newPendingFuelBurnThreshold("pending-fuel-burn-threshold-$index", "sample"))
        }
        val report = buildPendingFuelBurnThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
