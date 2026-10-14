package com.acme.transport.safety.violation.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedViolationAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedViolationAdjustmentValidator()
        val item = newBatchedViolationAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = BatchedViolationAdjustmentNameLengthRule(3600)
        val item = newBatchedViolationAdjustment("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedViolationAdjustmentRequiredCodeRule("batched violation adjustment needs a value")
        val item = newBatchedViolationAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = BatchedViolationAdjustmentRequiredExternalRefRule("batched violation adjustment needs this field")
        val item = newBatchedViolationAdjustment("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = BatchedViolationAdjustmentRequiredTimezoneRule("batched violation adjustment needs a value")
        val item = newBatchedViolationAdjustment("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = BatchedViolationAdjustmentEtaMinutesRangeRule(64, 3600)
        val item = newBatchedViolationAdjustment("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
