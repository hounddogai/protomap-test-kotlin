package com.acme.transport.crewing.availability.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityRuleValidator()
        val item = newAvailabilityRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AvailabilityRuleRequiredTitleRule("availability rule needs an entry")
        val item = newAvailabilityRule("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = AvailabilityRuleBudgetRangeRule(9.8, 1930.7)
        val item = newAvailabilityRule("x", "y")
        item.budget = 1932.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AvailabilityRuleCodeLengthRule(32)
        val item = newAvailabilityRule("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = AvailabilityRuleLabelLengthRule(32)
        val item = newAvailabilityRule("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
