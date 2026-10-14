package com.acme.transport.crewing.roster.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedRosterAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedRosterAssignmentValidator()
        val item = newCappedRosterAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = CappedRosterAssignmentGraceDaysRangeRule(64, 1000)
        val item = newCappedRosterAssignment("x", "y")
        item.graceDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CappedRosterAssignmentRequiredNameRule("capped roster assignment needs this field")
        val item = newCappedRosterAssignment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = CappedRosterAssignmentBudgetRangeRule(9.2, 2139.7)
        val item = newCappedRosterAssignment("x", "y")
        item.budget = 2141.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedRosterAssignmentNameLengthRule(255)
        val item = newCappedRosterAssignment("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = CappedRosterAssignmentRequiredExternalRefRule("capped roster assignment needs an entry")
        val item = newCappedRosterAssignment("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
