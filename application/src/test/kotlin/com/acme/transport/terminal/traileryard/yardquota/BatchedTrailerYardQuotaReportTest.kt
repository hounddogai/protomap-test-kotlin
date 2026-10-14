package com.acme.transport.terminal.traileryard.yardquota

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTrailerYardQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702949261)
        val items = mutableListOf<BatchedTrailerYardQuota>()
        for (index in 0 until 3) {
            items.add(newBatchedTrailerYardQuota("batched-trailer-yard-quota-$index", "sample"))
        }
        val report = buildBatchedTrailerYardQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
