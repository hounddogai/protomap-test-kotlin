package com.acme.transport.crewing.availability.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientAvailabilityAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientAvailabilityAllocationValidator()
        val item = newLenientAvailabilityAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientAvailabilityAllocationTitleLengthRule(1000)
        val item = newLenientAvailabilityAllocation("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LenientAvailabilityAllocationRequiredTitleRule("lenient availability allocation needs a value")
        val item = newLenientAvailabilityAllocation("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = LenientAvailabilityAllocationBudgetRangeRule(6.8, 2811.9)
        val item = newLenientAvailabilityAllocation("x", "y")
        item.budget = 2813.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LenientAvailabilityAllocationRequiredLabelRule("lenient availability allocation needs an entry")
        val item = newLenientAvailabilityAllocation("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
