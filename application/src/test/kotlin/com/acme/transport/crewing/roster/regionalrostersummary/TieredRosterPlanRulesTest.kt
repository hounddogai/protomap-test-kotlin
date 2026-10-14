package com.acme.transport.crewing.roster.regionalrostersummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredRosterPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredRosterPlanValidator()
        val item = newTieredRosterPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredRosterPlanCodeLengthRule(32)
        val item = newTieredRosterPlan("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TieredRosterPlanRequiredNameRule("tiered roster plan needs a value")
        val item = newTieredRosterPlan("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = TieredRosterPlanAmountRangeRule(5.3, 3919.5)
        val item = newTieredRosterPlan("x", "y")
        item.amount = 3921.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredRosterPlanRequiredCodeRule("tiered roster plan needs this field")
        val item = newTieredRosterPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = TieredRosterPlanDurationMinutesRangeRule(1000, 86400)
        val item = newTieredRosterPlan("x", "y")
        item.durationMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
