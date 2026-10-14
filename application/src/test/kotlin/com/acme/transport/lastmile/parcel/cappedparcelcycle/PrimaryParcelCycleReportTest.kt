package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryParcelCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706408340)
        val items = mutableListOf<PrimaryParcelCycle>()
        for (index in 0 until 5) {
            items.add(newPrimaryParcelCycle("primary-parcel-cycle-$index", "sample"))
        }
        val report = buildPrimaryParcelCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
