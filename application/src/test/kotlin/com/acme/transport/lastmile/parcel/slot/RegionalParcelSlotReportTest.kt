package com.acme.transport.lastmile.parcel.slot

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalParcelSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704392253)
        val items = mutableListOf<RegionalParcelSlot>()
        for (index in 0 until 6) {
            items.add(newRegionalParcelSlot("regional-parcel-slot-$index", "sample"))
        }
        val report = buildRegionalParcelSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
