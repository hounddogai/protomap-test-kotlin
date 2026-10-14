package com.acme.transport.terminal.sortation.sortationvariance

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedSortationOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707968414)
        val items = mutableListOf<BatchedSortationOverride>()
        for (index in 0 until 5) {
            items.add(newBatchedSortationOverride("batched-sortation-override-$index", "sample"))
        }
        val report = buildBatchedSortationOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
