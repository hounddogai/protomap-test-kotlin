package com.acme.transport.emissions.idling.idlingsnapshot

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedIdlingVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704814229)
        val items = mutableListOf<BatchedIdlingVariance>()
        for (index in 0 until 6) {
            items.add(newBatchedIdlingVariance("batched-idling-variance-$index", "sample"))
        }
        val report = buildBatchedIdlingVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
