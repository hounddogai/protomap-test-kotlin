package com.acme.transport.crewing.swap.swapschedule

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707668027)
        val items = mutableListOf<SwapCycle>()
        for (index in 0 until 2) {
            items.add(newSwapCycle("swap-cycle-$index", "sample"))
        }
        val report = buildSwapCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
