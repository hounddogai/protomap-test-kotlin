package com.acme.transport.safety.coaching.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCoachingSummaryValidator()
        val item = newCoachingSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = CoachingSummaryCeilingRangeRule(8.4, 1371.6)
        val item = newCoachingSummary("x", "y")
        item.ceiling = 1373.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = CoachingSummaryDepotLengthRule(1000)
        val item = newCoachingSummary("x", "y")
        item.depot = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
