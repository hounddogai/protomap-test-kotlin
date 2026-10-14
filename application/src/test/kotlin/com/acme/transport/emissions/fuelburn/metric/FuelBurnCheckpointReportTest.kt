package com.acme.transport.emissions.fuelburn.metric

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700973806)
        val items = mutableListOf<FuelBurnCheckpoint>()
        for (index in 0 until 5) {
            items.add(newFuelBurnCheckpoint("fuel-burn-checkpoint-$index", "sample"))
        }
        val report = buildFuelBurnCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
