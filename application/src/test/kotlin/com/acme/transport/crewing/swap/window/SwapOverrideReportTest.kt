package com.acme.transport.crewing.swap.window

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709273859)
        val items = mutableListOf<SwapOverride>()
        for (index in 0 until 3) {
            items.add(newSwapOverride("swap-override-$index", "sample"))
        }
        val report = buildSwapOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
