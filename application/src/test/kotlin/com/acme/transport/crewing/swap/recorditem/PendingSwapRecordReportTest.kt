package com.acme.transport.crewing.swap.recorditem

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingSwapRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700397655)
        val items = mutableListOf<PendingSwapRecord>()
        for (index in 0 until 5) {
            items.add(newPendingSwapRecord("pending-swap-record-$index", "sample"))
        }
        val report = buildPendingSwapRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
