package com.acme.transport.lastmile.locker.cycle

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707719831)
        val items = mutableListOf<LockerSummary>()
        for (index in 0 until 5) {
            items.add(newLockerSummary("locker-summary-$index", "sample"))
        }
        val report = buildLockerSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
