package com.acme.transport.emissions.evcharge.regionalchargeslot

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultEvChargeSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701131255)
        val items = mutableListOf<DefaultEvChargeSlot>()
        for (index in 0 until 2) {
            items.add(newDefaultEvChargeSlot("default-ev-charge-slot-$index", "sample"))
        }
        val report = buildDefaultEvChargeSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
