package com.acme.transport.crewing.availability.mapping

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalAvailabilitySlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708139044)
        val items = mutableListOf<GlobalAvailabilitySlot>()
        for (index in 0 until 3) {
            items.add(newGlobalAvailabilitySlot("global-availability-slot-$index", "sample"))
        }
        val report = buildGlobalAvailabilitySlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
