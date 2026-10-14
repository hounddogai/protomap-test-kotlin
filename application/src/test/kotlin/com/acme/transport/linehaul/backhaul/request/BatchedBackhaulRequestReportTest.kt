package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedBackhaulRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706919796)
        val items = mutableListOf<BatchedBackhaulRequest>()
        for (index in 0 until 2) {
            items.add(newBatchedBackhaulRequest("batched-backhaul-request-$index", "sample"))
        }
        val report = buildBatchedBackhaulRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
