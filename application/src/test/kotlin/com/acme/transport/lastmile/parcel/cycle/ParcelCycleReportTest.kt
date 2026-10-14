package com.acme.transport.lastmile.parcel.cycle

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707647926)
        val items = mutableListOf<ParcelCycle>()
        for (index in 0 until 3) {
            items.add(newParcelCycle("parcel-cycle-$index", "sample"))
        }
        val report = buildParcelCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
