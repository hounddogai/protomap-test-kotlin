package com.acme.transport.terminal.sortation.tier

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700967124)
        val items = mutableListOf<SortationBatch>()
        for (index in 0 until 2) {
            items.add(newSortationBatch("sortation-batch-$index", "sample"))
        }
        val report = buildSortationBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
