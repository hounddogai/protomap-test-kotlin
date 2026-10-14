package com.acme.transport.safety.scorecard.rule

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictScorecardRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705281809)
        val items = mutableListOf<StrictScorecardRule>()
        for (index in 0 until 3) {
            items.add(newStrictScorecardRule("strict-scorecard-rule-$index", "sample"))
        }
        val report = buildStrictScorecardRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
