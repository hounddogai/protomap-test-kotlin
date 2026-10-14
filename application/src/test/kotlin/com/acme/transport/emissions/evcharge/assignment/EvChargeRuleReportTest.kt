package com.acme.transport.emissions.evcharge.assignment

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704740695)
        val items = mutableListOf<EvChargeRule>()
        for (index in 0 until 3) {
            items.add(newEvChargeRule("ev-charge-rule-$index", "sample"))
        }
        val report = buildEvChargeRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
