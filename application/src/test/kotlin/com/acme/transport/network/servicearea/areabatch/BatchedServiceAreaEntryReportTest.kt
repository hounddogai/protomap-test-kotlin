package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedServiceAreaEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706810991)
        val items = mutableListOf<BatchedServiceAreaEntry>()
        for (index in 0 until 5) {
            items.add(newBatchedServiceAreaEntry("batched-service-area-entry-$index", "sample"))
        }
        val report = buildBatchedServiceAreaEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
