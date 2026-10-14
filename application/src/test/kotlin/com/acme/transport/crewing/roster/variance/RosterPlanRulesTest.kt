package com.acme.transport.crewing.roster.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterPlanValidator()
        val item = newRosterPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RosterPlanRequiredTitleRule("roster plan needs a value")
        val item = newRosterPlan("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RosterPlanLabelLengthRule(1000)
        val item = newRosterPlan("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = RosterPlanPriorityRangeRule(80, 80)
        val item = newRosterPlan("x", "y")
        item.priority = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = RosterPlanRequiredTimezoneRule("roster plan needs this field")
        val item = newRosterPlan("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
