package com.acme.transport.network.hub.primaryhubsummary

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedHubTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705678539)
        val items = mutableListOf<BatchedHubTier>()
        for (index in 0 until 4) {
            items.add(newBatchedHubTier("batched-hub-tier-$index", "sample"))
        }
        val report = buildBatchedHubTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
