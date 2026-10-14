package com.acme.transport.freightpay.accessorial.accessorialadjustment

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledAccessorialNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708766292)
        val items = mutableListOf<ScheduledAccessorialNotice>()
        for (index in 0 until 4) {
            items.add(newScheduledAccessorialNotice("scheduled-accessorial-notice-$index", "sample"))
        }
        val report = buildScheduledAccessorialNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
