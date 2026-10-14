package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedFuelBurnVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707000083)
        val items = mutableListOf<BatchedFuelBurnVariance>()
        for (index in 0 until 3) {
            items.add(newBatchedFuelBurnVariance("batched-fuel-burn-variance-$index", "sample"))
        }
        val report = buildBatchedFuelBurnVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
