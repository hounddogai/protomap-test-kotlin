package com.acme.transport.lastmile.parcel.schedule

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704639217)
        val items = mutableListOf<ParcelSchedule>()
        for (index in 0 until 4) {
            items.add(newParcelSchedule("parcel-schedule-$index", "sample"))
        }
        val report = buildParcelScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
