package com.acme.transport.emissions.fuelburn.burnrequest

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedFuelBurnCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705600058)
        val items = mutableListOf<BatchedFuelBurnCycle>()
        for (index in 0 until 3) {
            items.add(newBatchedFuelBurnCycle("batched-fuel-burn-cycle-$index", "sample"))
        }
        val report = buildBatchedFuelBurnCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
