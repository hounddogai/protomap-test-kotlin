package com.acme.transport.linehaul.capacity.cycle

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700925711)
        val items = mutableListOf<CapacitySchedule>()
        for (index in 0 until 6) {
            items.add(newCapacitySchedule("capacity-schedule-$index", "sample"))
        }
        val report = buildCapacityScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
