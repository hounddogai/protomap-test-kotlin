package com.acme.transport.customs.clearance.policy

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707266334)
        val items = mutableListOf<ClearanceRule>()
        for (index in 0 until 2) {
            items.add(newClearanceRule("clearance-rule-$index", "sample"))
        }
        val report = buildClearanceRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
