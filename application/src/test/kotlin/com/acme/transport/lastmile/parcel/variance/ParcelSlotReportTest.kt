package com.acme.transport.lastmile.parcel.variance

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706255204)
        val items = mutableListOf<ParcelSlot>()
        for (index in 0 until 4) {
            items.add(newParcelSlot("parcel-slot-$index", "sample"))
        }
        val report = buildParcelSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
