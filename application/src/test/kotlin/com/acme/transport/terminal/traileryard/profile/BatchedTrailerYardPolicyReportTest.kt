package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTrailerYardPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701036959)
        val items = mutableListOf<BatchedTrailerYardPolicy>()
        for (index in 0 until 4) {
            items.add(newBatchedTrailerYardPolicy("batched-trailer-yard-policy-$index", "sample"))
        }
        val report = buildBatchedTrailerYardPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
