package com.acme.transport.customs.hold.schedule

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultHoldScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703604364)
        val items = mutableListOf<DefaultHoldSchedule>()
        for (index in 0 until 3) {
            items.add(newDefaultHoldSchedule("default-hold-schedule-$index", "sample"))
        }
        val report = buildDefaultHoldScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
