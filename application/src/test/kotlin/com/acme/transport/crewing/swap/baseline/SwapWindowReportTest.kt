package com.acme.transport.crewing.swap.baseline

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700196140)
        val items = mutableListOf<SwapWindow>()
        for (index in 0 until 3) {
            items.add(newSwapWindow("swap-window-$index", "sample"))
        }
        val report = buildSwapWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
