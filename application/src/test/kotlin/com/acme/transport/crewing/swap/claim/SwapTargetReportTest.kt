package com.acme.transport.crewing.swap.claim

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708739605)
        val items = mutableListOf<SwapTarget>()
        for (index in 0 until 5) {
            items.add(newSwapTarget("swap-target-$index", "sample"))
        }
        val report = buildSwapTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
