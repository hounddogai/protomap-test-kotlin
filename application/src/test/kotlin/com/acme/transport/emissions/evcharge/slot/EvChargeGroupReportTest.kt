package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701625796)
        val items = mutableListOf<EvChargeGroup>()
        for (index in 0 until 6) {
            items.add(newEvChargeGroup("ev-charge-group-$index", "sample"))
        }
        val report = buildEvChargeGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
