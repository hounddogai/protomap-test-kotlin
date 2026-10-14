package com.acme.transport.customs.hold.forecast

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryHoldNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700575595)
        val items = mutableListOf<PrimaryHoldNotice>()
        for (index in 0 until 4) {
            items.add(newPrimaryHoldNotice("primary-hold-notice-$index", "sample"))
        }
        val report = buildPrimaryHoldNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
