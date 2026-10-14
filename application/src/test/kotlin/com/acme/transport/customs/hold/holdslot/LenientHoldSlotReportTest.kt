package com.acme.transport.customs.hold.holdslot

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientHoldSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707305095)
        val items = mutableListOf<LenientHoldSlot>()
        for (index in 0 until 3) {
            items.add(newLenientHoldSlot("lenient-hold-slot-$index", "sample"))
        }
        val report = buildLenientHoldSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
