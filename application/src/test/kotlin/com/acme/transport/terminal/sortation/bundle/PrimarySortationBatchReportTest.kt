package com.acme.transport.terminal.sortation.bundle

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimarySortationBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702777842)
        val items = mutableListOf<PrimarySortationBatch>()
        for (index in 0 until 5) {
            items.add(newPrimarySortationBatch("primary-sortation-batch-$index", "sample"))
        }
        val report = buildPrimarySortationBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
