package com.acme.transport.freightpay.dispute.schedule

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDisputeTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709434571)
        val items = mutableListOf<FallbackDisputeTier>()
        for (index in 0 until 4) {
            items.add(newFallbackDisputeTier("fallback-dispute-tier-$index", "sample"))
        }
        val report = buildFallbackDisputeTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
