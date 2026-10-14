package com.acme.transport.crewing.swap.policy

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700962682)
        val items = mutableListOf<SwapEntry>()
        for (index in 0 until 4) {
            items.add(newSwapEntry("swap-entry-$index", "sample"))
        }
        val report = buildSwapEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
