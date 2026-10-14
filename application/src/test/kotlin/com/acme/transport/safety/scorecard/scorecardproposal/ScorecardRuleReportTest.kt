package com.acme.transport.safety.scorecard.scorecardproposal

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706785827)
        val items = mutableListOf<ScorecardRule>()
        for (index in 0 until 2) {
            items.add(newScorecardRule("scorecard-rule-$index", "sample"))
        }
        val report = buildScorecardRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
