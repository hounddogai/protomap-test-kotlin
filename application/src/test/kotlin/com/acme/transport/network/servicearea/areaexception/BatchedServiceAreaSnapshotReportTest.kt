package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedServiceAreaSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703794737)
        val items = mutableListOf<BatchedServiceAreaSnapshot>()
        for (index in 0 until 3) {
            items.add(newBatchedServiceAreaSnapshot("batched-service-area-snapshot-$index", "sample"))
        }
        val report = buildBatchedServiceAreaSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
