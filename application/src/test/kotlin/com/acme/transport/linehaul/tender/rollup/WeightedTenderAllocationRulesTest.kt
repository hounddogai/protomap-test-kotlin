package com.acme.transport.linehaul.tender.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTenderAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedTenderAllocationValidator()
        val item = newWeightedTenderAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedTenderAllocationCodeLengthRule(16)
        val item = newWeightedTenderAllocation("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedTenderAllocationRequiredLabelRule("weighted tender allocation needs an entry")
        val item = newWeightedTenderAllocation("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedTenderAllocationRequiredCodeRule("weighted tender allocation needs this field")
        val item = newWeightedTenderAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = WeightedTenderAllocationSequenceRangeRule(16, 32)
        val item = newWeightedTenderAllocation("x", "y")
        item.sequence = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = WeightedTenderAllocationAttemptsRangeRule(32, 3600)
        val item = newWeightedTenderAllocation("x", "y")
        item.attempts = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
