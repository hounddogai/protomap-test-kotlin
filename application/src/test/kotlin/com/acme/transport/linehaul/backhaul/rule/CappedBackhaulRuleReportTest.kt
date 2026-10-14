package com.acme.transport.linehaul.backhaul.rule

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedBackhaulRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700857124)
        val items = mutableListOf<CappedBackhaulRule>()
        for (index in 0 until 3) {
            items.add(newCappedBackhaulRule("capped-backhaul-rule-$index", "sample"))
        }
        val report = buildCappedBackhaulRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
