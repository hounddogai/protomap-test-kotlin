package com.acme.transport.crewing.swap.swapschedule

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackSwapPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704914100)
        val items = mutableListOf<FallbackSwapPolicy>()
        for (index in 0 until 5) {
            items.add(newFallbackSwapPolicy("fallback-swap-policy-$index", "sample"))
        }
        val report = buildFallbackSwapPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
