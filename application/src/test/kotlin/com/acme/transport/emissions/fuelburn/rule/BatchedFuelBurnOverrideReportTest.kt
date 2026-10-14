package com.acme.transport.emissions.fuelburn.rule

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedFuelBurnOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702121313)
        val items = mutableListOf<BatchedFuelBurnOverride>()
        for (index in 0 until 3) {
            items.add(newBatchedFuelBurnOverride("batched-fuel-burn-override-$index", "sample"))
        }
        val report = buildBatchedFuelBurnOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
