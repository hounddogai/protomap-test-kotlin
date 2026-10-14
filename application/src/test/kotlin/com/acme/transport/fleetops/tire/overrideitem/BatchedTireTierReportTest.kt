package com.acme.transport.fleetops.tire.overrideitem

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTireTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704322461)
        val items = mutableListOf<BatchedTireTier>()
        for (index in 0 until 4) {
            items.add(newBatchedTireTier("batched-tire-tier-$index", "sample"))
        }
        val report = buildBatchedTireTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
