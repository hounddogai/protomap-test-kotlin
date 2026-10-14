package com.acme.transport.crewing.roster.rostermilestone

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRosterRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706882945)
        val items = mutableListOf<LenientRosterRule>()
        for (index in 0 until 2) {
            items.add(newLenientRosterRule("lenient-roster-rule-$index", "sample"))
        }
        val report = buildLenientRosterRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
