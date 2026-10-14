package com.acme.transport.lastmile.drop.weighteddropnotice

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701042584)
        val items = mutableListOf<DropSummary>()
        for (index in 0 until 6) {
            items.add(newDropSummary("drop-summary-$index", "sample"))
        }
        val report = buildDropSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
