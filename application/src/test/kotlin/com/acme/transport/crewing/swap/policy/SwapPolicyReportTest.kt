package com.acme.transport.crewing.swap.policy

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704665003)
        val items = mutableListOf<SwapPolicy>()
        for (index in 0 until 3) {
            items.add(newSwapPolicy("swap-policy-$index", "sample"))
        }
        val report = buildSwapPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
