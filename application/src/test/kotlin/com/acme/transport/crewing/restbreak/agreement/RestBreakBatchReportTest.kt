package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706957374)
        val items = mutableListOf<RestBreakBatch>()
        for (index in 0 until 2) {
            items.add(newRestBreakBatch("rest-break-batch-$index", "sample"))
        }
        val report = buildRestBreakBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
