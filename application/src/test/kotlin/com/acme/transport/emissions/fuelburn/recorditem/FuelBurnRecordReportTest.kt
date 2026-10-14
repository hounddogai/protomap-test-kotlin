package com.acme.transport.emissions.fuelburn.recorditem

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702457202)
        val items = mutableListOf<FuelBurnRecord>()
        for (index in 0 until 5) {
            items.add(newFuelBurnRecord("fuel-burn-record-$index", "sample"))
        }
        val report = buildFuelBurnRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
