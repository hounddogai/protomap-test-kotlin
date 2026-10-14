package com.acme.transport.safety.coaching.segment

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictCoachingScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703814659)
        val items = mutableListOf<StrictCoachingSchedule>()
        for (index in 0 until 5) {
            items.add(newStrictCoachingSchedule("strict-coaching-schedule-$index", "sample"))
        }
        val report = buildStrictCoachingScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
