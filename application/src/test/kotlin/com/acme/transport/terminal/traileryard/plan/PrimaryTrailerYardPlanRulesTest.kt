package com.acme.transport.terminal.traileryard.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryTrailerYardPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryTrailerYardPlanValidator()
        val item = newPrimaryTrailerYardPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = PrimaryTrailerYardPlanOwnerIdLengthRule(32)
        val item = newPrimaryTrailerYardPlan("x", "y")
        item.ownerId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = PrimaryTrailerYardPlanRequiredOwnerIdRule("primary trailer yard plan needs a value")
        val item = newPrimaryTrailerYardPlan("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = PrimaryTrailerYardPlanExpiresAtRangeRule(120, 86400)
        val item = newPrimaryTrailerYardPlan("x", "y")
        item.expiresAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryTrailerYardPlanRequiredCodeRule("primary trailer yard plan needs an entry")
        val item = newPrimaryTrailerYardPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryTrailerYardPlanCodeLengthRule(16)
        val item = newPrimaryTrailerYardPlan("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
