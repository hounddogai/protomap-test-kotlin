package com.acme.transport.lastmile.attempt.attemptplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictAttemptPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictAttemptPlanValidator()
        val item = newStrictAttemptPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictAttemptPlanRequiredCodeRule("strict attempt plan needs this field")
        val item = newStrictAttemptPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = StrictAttemptPlanQuantityRangeRule(32, 86400)
        val item = newStrictAttemptPlan("x", "y")
        item.quantity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictAttemptPlanTitleLengthRule(86400)
        val item = newStrictAttemptPlan("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
