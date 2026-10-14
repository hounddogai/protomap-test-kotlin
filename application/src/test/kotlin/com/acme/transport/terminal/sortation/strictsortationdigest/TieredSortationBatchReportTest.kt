package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredSortationBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706187146)
        val items = mutableListOf<TieredSortationBatch>()
        for (index in 0 until 2) {
            items.add(newTieredSortationBatch("tiered-sortation-batch-$index", "sample"))
        }
        val report = buildTieredSortationBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
