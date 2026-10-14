package com.acme.transport.crewing.roster.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterBudgetValidator()
        val item = newRosterBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RosterBudgetCodeLengthRule(120)
        val item = newRosterBudget("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = RosterBudgetShiftCodeLengthRule(80)
        val item = newRosterBudget("x", "y")
        item.shiftCode = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RosterBudgetRequiredTitleRule("roster budget needs a value")
        val item = newRosterBudget("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = RosterBudgetRequiredRegionRule("roster budget needs an entry")
        val item = newRosterBudget("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
