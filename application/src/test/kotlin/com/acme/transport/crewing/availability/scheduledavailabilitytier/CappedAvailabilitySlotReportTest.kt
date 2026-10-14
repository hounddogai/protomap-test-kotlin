package com.acme.transport.crewing.availability.scheduledavailabilitytier

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAvailabilitySlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702820348)
        val items = mutableListOf<CappedAvailabilitySlot>()
        for (index in 0 until 5) {
            items.add(newCappedAvailabilitySlot("capped-availability-slot-$index", "sample"))
        }
        val report = buildCappedAvailabilitySlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
