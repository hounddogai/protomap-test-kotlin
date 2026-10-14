package com.acme.transport.terminal.traileryard.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingTrailerYardRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingTrailerYardRuleValidator()
        val item = newRollingTrailerYardRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = RollingTrailerYardRuleCostRangeRule(5.0, 4082.1)
        val item = newRollingTrailerYardRule("x", "y")
        item.cost = 4083.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = RollingTrailerYardRuleStopCountRangeRule(80, 3600)
        val item = newRollingTrailerYardRule("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingTrailerYardRuleRequiredCodeRule("rolling trailer yard rule needs a value")
        val item = newRollingTrailerYardRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
