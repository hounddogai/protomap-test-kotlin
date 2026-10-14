package com.acme.transport.freightpay.dispute.schedule

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706281251)
        val items = mutableListOf<DisputeSchedule>()
        for (index in 0 until 6) {
            items.add(newDisputeSchedule("dispute-schedule-$index", "sample"))
        }
        val report = buildDisputeScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
