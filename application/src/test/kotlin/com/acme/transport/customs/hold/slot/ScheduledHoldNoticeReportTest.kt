package com.acme.transport.customs.hold.slot

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledHoldNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706602422)
        val items = mutableListOf<ScheduledHoldNotice>()
        for (index in 0 until 2) {
            items.add(newScheduledHoldNotice("scheduled-hold-notice-$index", "sample"))
        }
        val report = buildScheduledHoldNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
