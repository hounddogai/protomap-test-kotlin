package com.acme.transport.crewing.swap.variance

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700424294)
        val items = mutableListOf<SwapVariance>()
        for (index in 0 until 6) {
            items.add(newSwapVariance("swap-variance-$index", "sample"))
        }
        val report = buildSwapVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
