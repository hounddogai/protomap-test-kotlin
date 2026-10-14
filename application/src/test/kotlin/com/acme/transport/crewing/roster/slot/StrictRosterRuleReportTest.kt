package com.acme.transport.crewing.roster.slot

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRosterRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707598496)
        val items = mutableListOf<StrictRosterRule>()
        for (index in 0 until 2) {
            items.add(newStrictRosterRule("strict-roster-rule-$index", "sample"))
        }
        val report = buildStrictRosterRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
