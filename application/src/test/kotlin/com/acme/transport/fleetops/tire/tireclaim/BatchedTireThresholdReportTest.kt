package com.acme.transport.fleetops.tire.tireclaim

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTireThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707119581)
        val items = mutableListOf<BatchedTireThreshold>()
        for (index in 0 until 6) {
            items.add(newBatchedTireThreshold("batched-tire-threshold-$index", "sample"))
        }
        val report = buildBatchedTireThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
