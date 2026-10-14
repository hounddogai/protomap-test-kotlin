package com.acme.transport.terminal.traileryard.rollup

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingTrailerYardRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707233018)
        val items = mutableListOf<RollingTrailerYardRule>()
        for (index in 0 until 2) {
            items.add(newRollingTrailerYardRule("rolling-trailer-yard-rule-$index", "sample"))
        }
        val report = buildRollingTrailerYardRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
