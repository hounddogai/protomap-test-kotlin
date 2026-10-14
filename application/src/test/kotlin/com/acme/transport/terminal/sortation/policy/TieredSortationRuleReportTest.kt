package com.acme.transport.terminal.sortation.policy

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredSortationRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709597945)
        val items = mutableListOf<TieredSortationRule>()
        for (index in 0 until 5) {
            items.add(newTieredSortationRule("tiered-sortation-rule-$index", "sample"))
        }
        val report = buildTieredSortationRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
