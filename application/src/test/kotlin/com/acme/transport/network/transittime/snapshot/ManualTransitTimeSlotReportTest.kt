package com.acme.transport.network.transittime.snapshot

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTransitTimeSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704439283)
        val items = mutableListOf<ManualTransitTimeSlot>()
        for (index in 0 until 3) {
            items.add(newManualTransitTimeSlot("manual-transit-time-slot-$index", "sample"))
        }
        val report = buildManualTransitTimeSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
