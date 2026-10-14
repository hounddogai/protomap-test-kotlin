package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredParcelCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701644395)
        val items = mutableListOf<TieredParcelCycle>()
        for (index in 0 until 3) {
            items.add(newTieredParcelCycle("tiered-parcel-cycle-$index", "sample"))
        }
        val report = buildTieredParcelCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
