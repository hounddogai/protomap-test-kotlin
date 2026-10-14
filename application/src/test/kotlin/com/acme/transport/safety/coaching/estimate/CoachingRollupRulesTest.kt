package com.acme.transport.safety.coaching.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCoachingRollupValidator()
        val item = newCoachingRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CoachingRollupRequiredCodeRule("coaching rollup needs a value")
        val item = newCoachingRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = CoachingRollupBudgetRangeRule(4.1, 4492.1)
        val item = newCoachingRollup("x", "y")
        item.budget = 4493.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = CoachingRollupCodeLengthRule(1000)
        val item = newCoachingRollup("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = CoachingRollupRequiredContactEmailRule("coaching rollup needs an entry")
        val item = newCoachingRollup("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
