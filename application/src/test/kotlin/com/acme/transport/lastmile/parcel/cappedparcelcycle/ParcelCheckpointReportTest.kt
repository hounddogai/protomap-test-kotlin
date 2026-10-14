package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708384414)
        val items = mutableListOf<ParcelCheckpoint>()
        for (index in 0 until 3) {
            items.add(newParcelCheckpoint("parcel-checkpoint-$index", "sample"))
        }
        val report = buildParcelCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
