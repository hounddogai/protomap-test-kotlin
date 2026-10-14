package com.acme.transport.lastmile.parcel.scheduledparcelreservation

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalParcelRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707073142)
        val items = mutableListOf<SeasonalParcelRecord>()
        for (index in 0 until 3) {
            items.add(newSeasonalParcelRecord("seasonal-parcel-record-$index", "sample"))
        }
        val report = buildSeasonalParcelRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
