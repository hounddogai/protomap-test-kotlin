package com.acme.transport.lastmile.redelivery.threshold

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704833447)
        val items = mutableListOf<RedeliveryNotice>()
        for (index in 0 until 3) {
            items.add(newRedeliveryNotice("redelivery-notice-$index", "sample"))
        }
        val report = buildRedeliveryNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
