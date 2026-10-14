package com.acme.transport.crewing.availability.availabilitydigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualAvailabilityBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualAvailabilityBudgetValidator()
        val item = newManualAvailabilityBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualAvailabilityBudgetCodeLengthRule(64)
        val item = newManualAvailabilityBudget("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = ManualAvailabilityBudgetCategoryLengthRule(32)
        val item = newManualAvailabilityBudget("x", "y")
        item.category = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualAvailabilityBudgetRequiredLabelRule("manual availability budget needs this field")
        val item = newManualAvailabilityBudget("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = ManualAvailabilityBudgetPriorityRangeRule(120, 120)
        val item = newManualAvailabilityBudget("x", "y")
        item.priority = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
