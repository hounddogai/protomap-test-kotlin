package com.acme.transport.terminal.dwell.slot

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDwellRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708130825)
        val items = mutableListOf<FallbackDwellRule>()
        for (index in 0 until 6) {
            items.add(newFallbackDwellRule("fallback-dwell-rule-$index", "sample"))
        }
        val report = buildFallbackDwellRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
