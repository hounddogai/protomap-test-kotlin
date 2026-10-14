package com.acme.transport.network.zone.zonebundle

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedZoneVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705586948)
        val items = mutableListOf<BatchedZoneVariance>()
        for (index in 0 until 5) {
            items.add(newBatchedZoneVariance("batched-zone-variance-$index", "sample"))
        }
        val report = buildBatchedZoneVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
