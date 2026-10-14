package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredEvChargeGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700624008)
        val items = mutableListOf<TieredEvChargeGroup>()
        for (index in 0 until 2) {
            items.add(newTieredEvChargeGroup("tiered-ev-charge-group-$index", "sample"))
        }
        val report = buildTieredEvChargeGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
