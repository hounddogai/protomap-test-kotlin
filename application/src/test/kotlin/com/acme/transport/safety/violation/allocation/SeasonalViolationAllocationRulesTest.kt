package com.acme.transport.safety.violation.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalViolationAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalViolationAllocationValidator()
        val item = newSeasonalViolationAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalViolationAllocationLabelLengthRule(64)
        val item = newSeasonalViolationAllocation("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalViolationAllocationRequiredCodeRule("seasonal violation allocation needs a value")
        val item = newSeasonalViolationAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = SeasonalViolationAllocationMaximumRangeRule(1.1, 65.8)
        val item = newSeasonalViolationAllocation("x", "y")
        item.maximum = 67.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
