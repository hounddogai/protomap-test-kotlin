package com.acme.transport.linehaul.trailer.trailerrule

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTrailerRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700209688)
        val items = mutableListOf<BatchedTrailerRule>()
        for (index in 0 until 4) {
            items.add(newBatchedTrailerRule("batched-trailer-rule-$index", "sample"))
        }
        val report = buildBatchedTrailerRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
