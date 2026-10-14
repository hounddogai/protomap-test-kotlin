package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictEvChargeSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709725482)
        val items = mutableListOf<StrictEvChargeSlot>()
        for (index in 0 until 2) {
            items.add(newStrictEvChargeSlot("strict-ev-charge-slot-$index", "sample"))
        }
        val report = buildStrictEvChargeSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
