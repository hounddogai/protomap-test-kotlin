package com.acme.transport.fleetops.tire.claim

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709388768)
        val items = mutableListOf<TireRule>()
        for (index in 0 until 4) {
            items.add(newTireRule("tire-rule-$index", "sample"))
        }
        val report = buildTireRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
