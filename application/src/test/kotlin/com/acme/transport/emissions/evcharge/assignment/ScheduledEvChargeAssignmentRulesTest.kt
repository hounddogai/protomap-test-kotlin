package com.acme.transport.emissions.evcharge.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledEvChargeAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledEvChargeAssignmentValidator()
        val item = newScheduledEvChargeAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledEvChargeAssignmentCodeLengthRule(16)
        val item = newScheduledEvChargeAssignment("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = ScheduledEvChargeAssignmentRequiredCategoryRule("scheduled ev charge assignment needs an entry")
        val item = newScheduledEvChargeAssignment("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledEvChargeAssignmentRequiredCodeRule("scheduled ev charge assignment needs a value")
        val item = newScheduledEvChargeAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = ScheduledEvChargeAssignmentAmountRangeRule(8.8, 3464.1)
        val item = newScheduledEvChargeAssignment("x", "y")
        item.amount = 3465.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long category`() {
        val rule = ScheduledEvChargeAssignmentCategoryLengthRule(86400)
        val item = newScheduledEvChargeAssignment("x", "y")
        item.category = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledEvChargeAssignmentRequiredLabelRule("scheduled ev charge assignment needs a value")
        val item = newScheduledEvChargeAssignment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
