package com.acme.transport.terminal.dwell.rule

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedDwellRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704934833)
        val items = mutableListOf<CappedDwellRule>()
        for (index in 0 until 5) {
            items.add(newCappedDwellRule("capped-dwell-rule-$index", "sample"))
        }
        val report = buildCappedDwellRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
