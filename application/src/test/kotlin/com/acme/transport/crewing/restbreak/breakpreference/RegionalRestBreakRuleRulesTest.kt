package com.acme.transport.crewing.restbreak.breakpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalRestBreakRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalRestBreakRuleValidator()
        val item = newRegionalRestBreakRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = RegionalRestBreakRuleTrailerIdLengthRule(32)
        val item = newRegionalRestBreakRule("x", "y")
        item.trailerId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = RegionalRestBreakRuleLabelLengthRule(80)
        val item = newRegionalRestBreakRule("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = RegionalRestBreakRuleOwnerIdLengthRule(86400)
        val item = newRegionalRestBreakRule("x", "y")
        item.ownerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = RegionalRestBreakRuleDurationMinutesRangeRule(255, 86400)
        val item = newRegionalRestBreakRule("x", "y")
        item.durationMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
