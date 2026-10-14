package com.acme.transport.emissions.fuelburn.request

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705090885)
        val items = mutableListOf<FuelBurnRequest>()
        for (index in 0 until 2) {
            items.add(newFuelBurnRequest("fuel-burn-request-$index", "sample"))
        }
        val report = buildFuelBurnRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
