package com.acme.transport.crewing.swap.request

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledSwapRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707970452)
        val items = mutableListOf<ScheduledSwapRequest>()
        for (index in 0 until 4) {
            items.add(newScheduledSwapRequest("scheduled-swap-request-$index", "sample"))
        }
        val report = buildScheduledSwapRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
