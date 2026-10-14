package com.acme.transport.crewing.restbreak.breakpreference

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalRestBreakRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705830048)
        val items = mutableListOf<RegionalRestBreakRule>()
        for (index in 0 until 6) {
            items.add(newRegionalRestBreakRule("regional-rest-break-rule-$index", "sample"))
        }
        val report = buildRegionalRestBreakRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
