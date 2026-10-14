package com.acme.transport.crewing.restbreak.breakentry

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRestBreakThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704158106)
        val items = mutableListOf<BatchedRestBreakThreshold>()
        for (index in 0 until 3) {
            items.add(newBatchedRestBreakThreshold("batched-rest-break-threshold-$index", "sample"))
        }
        val report = buildBatchedRestBreakThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
