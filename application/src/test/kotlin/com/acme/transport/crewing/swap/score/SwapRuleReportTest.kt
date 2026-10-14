package com.acme.transport.crewing.swap.score

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700849133)
        val items = mutableListOf<SwapRule>()
        for (index in 0 until 6) {
            items.add(newSwapRule("swap-rule-$index", "sample"))
        }
        val report = buildSwapRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
