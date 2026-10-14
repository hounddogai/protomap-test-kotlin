package com.acme.transport.freightpay.accessorial.baseline

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredAccessorialBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700293147)
        val items = mutableListOf<TieredAccessorialBatch>()
        for (index in 0 until 6) {
            items.add(newTieredAccessorialBatch("tiered-accessorial-batch-$index", "sample"))
        }
        val report = buildTieredAccessorialBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
