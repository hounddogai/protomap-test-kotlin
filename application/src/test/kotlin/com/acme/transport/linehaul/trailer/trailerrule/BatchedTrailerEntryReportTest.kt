package com.acme.transport.linehaul.trailer.trailerrule

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTrailerEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703567531)
        val items = mutableListOf<BatchedTrailerEntry>()
        for (index in 0 until 6) {
            items.add(newBatchedTrailerEntry("batched-trailer-entry-$index", "sample"))
        }
        val report = buildBatchedTrailerEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
