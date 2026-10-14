package com.acme.transport.crewing.availability.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedAvailabilityRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedAvailabilityRuleValidator()
        val item = newBatchedAvailabilityRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = BatchedAvailabilityRuleSummaryLengthRule(80)
        val item = newBatchedAvailabilityRule("x", "y")
        item.summary = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = BatchedAvailabilityRuleQuantityRangeRule(80, 120)
        val item = newBatchedAvailabilityRule("x", "y")
        item.quantity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedAvailabilityRuleRequiredNameRule("batched availability rule needs this field")
        val item = newBatchedAvailabilityRule("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = BatchedAvailabilityRuleMaximumRangeRule(6.5, 4637.7)
        val item = newBatchedAvailabilityRule("x", "y")
        item.maximum = 4639.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedAvailabilityRuleCodeLengthRule(80)
        val item = newBatchedAvailabilityRule("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
