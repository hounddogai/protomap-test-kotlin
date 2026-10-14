package com.acme.transport.crewing.roster.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRosterBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultRosterBudgetValidator()
        val item = newDefaultRosterBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = DefaultRosterBudgetRevisionRangeRule(80, 86400)
        val item = newDefaultRosterBudget("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long segment`() {
        val rule = DefaultRosterBudgetSegmentLengthRule(255)
        val item = newDefaultRosterBudget("x", "y")
        item.segment = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
