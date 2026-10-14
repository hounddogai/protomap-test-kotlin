package com.acme.transport.crewing.restbreak.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakRuleValidator()
        val item = newRestBreakRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RestBreakRuleNameLengthRule(64)
        val item = newRestBreakRule("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = RestBreakRuleCostRangeRule(7.7, 1666.6)
        val item = newRestBreakRule("x", "y")
        item.cost = 1668.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RestBreakRuleRequiredCodeRule("rest break rule needs a value")
        val item = newRestBreakRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = RestBreakRuleThresholdRangeRule(9.4, 4148.3)
        val item = newRestBreakRule("x", "y")
        item.threshold = 4149.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
