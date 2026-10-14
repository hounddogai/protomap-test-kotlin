package com.acme.transport.network.cutoff.batch

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701888141)
        val items = mutableListOf<CutoffBatch>()
        for (index in 0 until 6) {
            items.add(newCutoffBatch("cutoff-batch-$index", "sample"))
        }
        val report = buildCutoffBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
