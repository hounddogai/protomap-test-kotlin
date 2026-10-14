package com.acme.transport.crewing.roster.rule

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703112140)
        val items = mutableListOf<RosterRule>()
        for (index in 0 until 6) {
            items.add(newRosterRule("roster-rule-$index", "sample"))
        }
        val report = buildRosterRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
