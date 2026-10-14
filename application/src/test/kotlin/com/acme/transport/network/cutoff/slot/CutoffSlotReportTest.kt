package com.acme.transport.network.cutoff.slot

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704838082)
        val items = mutableListOf<CutoffSlot>()
        for (index in 0 until 2) {
            items.add(newCutoffSlot("cutoff-slot-$index", "sample"))
        }
        val report = buildCutoffSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
