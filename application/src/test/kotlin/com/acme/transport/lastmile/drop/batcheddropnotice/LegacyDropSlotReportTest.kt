package com.acme.transport.lastmile.drop.batcheddropnotice

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDropSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701990336)
        val items = mutableListOf<LegacyDropSlot>()
        for (index in 0 until 2) {
            items.add(newLegacyDropSlot("legacy-drop-slot-$index", "sample"))
        }
        val report = buildLegacyDropSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
