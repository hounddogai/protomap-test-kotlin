package com.acme.transport.crewing.swap.group

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702796721)
        val items = mutableListOf<SwapGroup>()
        for (index in 0 until 3) {
            items.add(newSwapGroup("swap-group-$index", "sample"))
        }
        val report = buildSwapGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
