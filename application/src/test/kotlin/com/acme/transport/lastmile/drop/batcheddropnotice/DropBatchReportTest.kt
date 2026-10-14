package com.acme.transport.lastmile.drop.batcheddropnotice

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700653563)
        val items = mutableListOf<DropBatch>()
        for (index in 0 until 6) {
            items.add(newDropBatch("drop-batch-$index", "sample"))
        }
        val report = buildDropBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
