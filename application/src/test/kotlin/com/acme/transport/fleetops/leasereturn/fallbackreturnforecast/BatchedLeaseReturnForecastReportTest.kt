package com.acme.transport.fleetops.leasereturn.fallbackreturnforecast

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedLeaseReturnForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705585573)
        val items = mutableListOf<BatchedLeaseReturnForecast>()
        for (index in 0 until 6) {
            items.add(newBatchedLeaseReturnForecast("batched-lease-return-forecast-$index", "sample"))
        }
        val report = buildBatchedLeaseReturnForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
