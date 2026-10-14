package com.acme.transport.lastmile.redelivery.milestone

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700099147)
        val items = mutableListOf<RedeliverySchedule>()
        for (index in 0 until 3) {
            items.add(newRedeliverySchedule("redelivery-schedule-$index", "sample"))
        }
        val report = buildRedeliveryScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
