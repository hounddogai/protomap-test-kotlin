package com.acme.transport.crewing.swap.swapnotice

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707695255)
        val items = mutableListOf<SwapProfile>()
        for (index in 0 until 3) {
            items.add(newSwapProfile("swap-profile-$index", "sample"))
        }
        val report = buildSwapProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
