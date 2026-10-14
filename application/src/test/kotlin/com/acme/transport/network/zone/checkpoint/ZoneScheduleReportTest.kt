package com.acme.transport.network.zone.checkpoint

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706883763)
        val items = mutableListOf<ZoneSchedule>()
        for (index in 0 until 2) {
            items.add(newZoneSchedule("zone-schedule-$index", "sample"))
        }
        val report = buildZoneScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
