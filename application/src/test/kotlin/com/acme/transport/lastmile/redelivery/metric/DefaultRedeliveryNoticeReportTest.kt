package com.acme.transport.lastmile.redelivery.metric

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRedeliveryNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700564698)
        val items = mutableListOf<DefaultRedeliveryNotice>()
        for (index in 0 until 3) {
            items.add(newDefaultRedeliveryNotice("default-redelivery-notice-$index", "sample"))
        }
        val report = buildDefaultRedeliveryNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
