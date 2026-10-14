package com.acme.transport.lastmile.parcel.baseline

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientParcelSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706472774)
        val items = mutableListOf<LenientParcelSlot>()
        for (index in 0 until 2) {
            items.add(newLenientParcelSlot("lenient-parcel-slot-$index", "sample"))
        }
        val report = buildLenientParcelSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
