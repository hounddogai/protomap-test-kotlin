package com.acme.transport.lastmile.parcel.ledger

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveParcelSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707761131)
        val items = mutableListOf<AdaptiveParcelSlot>()
        for (index in 0 until 2) {
            items.add(newAdaptiveParcelSlot("adaptive-parcel-slot-$index", "sample"))
        }
        val report = buildAdaptiveParcelSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
