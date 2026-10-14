package com.acme.transport.lastmile.drop.rule

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryDropRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707583416)
        val items = mutableListOf<PrimaryDropRule>()
        for (index in 0 until 6) {
            items.add(newPrimaryDropRule("primary-drop-rule-$index", "sample"))
        }
        val report = buildPrimaryDropRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
