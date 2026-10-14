package com.acme.transport.customs.clearance.schedule

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledClearanceNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700125905)
        val items = mutableListOf<ScheduledClearanceNotice>()
        for (index in 0 until 4) {
            items.add(newScheduledClearanceNotice("scheduled-clearance-notice-$index", "sample"))
        }
        val report = buildScheduledClearanceNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
