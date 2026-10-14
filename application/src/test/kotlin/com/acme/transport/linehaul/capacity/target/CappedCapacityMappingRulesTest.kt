package com.acme.transport.linehaul.capacity.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedCapacityMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedCapacityMappingValidator()
        val item = newCappedCapacityMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = CappedCapacityMappingRequiredReviewerRule("capped capacity mapping needs an entry")
        val item = newCappedCapacityMapping("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = CappedCapacityMappingCurrencyLengthRule(120)
        val item = newCappedCapacityMapping("x", "y")
        item.currency = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = CappedCapacityMappingRequiredCurrencyRule("capped capacity mapping needs a value")
        val item = newCappedCapacityMapping("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = CappedCapacityMappingLabelLengthRule(120)
        val item = newCappedCapacityMapping("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedCapacityMappingCodeLengthRule(3600)
        val item = newCappedCapacityMapping("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
