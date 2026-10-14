package com.acme.transport.lastmile.parcel.allocation

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictParcelCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704758730)
        val items = mutableListOf<StrictParcelCycle>()
        for (index in 0 until 6) {
            items.add(newStrictParcelCycle("strict-parcel-cycle-$index", "sample"))
        }
        val report = buildStrictParcelCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
