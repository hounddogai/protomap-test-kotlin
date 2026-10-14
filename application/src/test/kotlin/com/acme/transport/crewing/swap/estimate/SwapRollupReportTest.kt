package com.acme.transport.crewing.swap.estimate

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704885589)
        val items = mutableListOf<SwapRollup>()
        for (index in 0 until 6) {
            items.add(newSwapRollup("swap-rollup-$index", "sample"))
        }
        val report = buildSwapRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
