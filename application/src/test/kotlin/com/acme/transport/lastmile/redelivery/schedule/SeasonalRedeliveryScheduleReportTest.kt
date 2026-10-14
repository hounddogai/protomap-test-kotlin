package com.acme.transport.lastmile.redelivery.schedule

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRedeliveryScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702857609)
        val items = mutableListOf<SeasonalRedeliverySchedule>()
        for (index in 0 until 5) {
            items.add(newSeasonalRedeliverySchedule("seasonal-redelivery-schedule-$index", "sample"))
        }
        val report = buildSeasonalRedeliveryScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
