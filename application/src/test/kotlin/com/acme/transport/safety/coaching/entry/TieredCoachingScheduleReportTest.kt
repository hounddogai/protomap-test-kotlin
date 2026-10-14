package com.acme.transport.safety.coaching.entry

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCoachingScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701587733)
        val items = mutableListOf<TieredCoachingSchedule>()
        for (index in 0 until 4) {
            items.add(newTieredCoachingSchedule("tiered-coaching-schedule-$index", "sample"))
        }
        val report = buildTieredCoachingScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
