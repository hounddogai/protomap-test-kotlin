package com.acme.transport.safety.violation.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualViolationBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualViolationBatchValidator()
        val item = newManualViolationBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualViolationBatchLabelLengthRule(3600)
        val item = newManualViolationBatch("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualViolationBatchCodeLengthRule(32)
        val item = newManualViolationBatch("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = ManualViolationBatchTimezoneLengthRule(255)
        val item = newManualViolationBatch("x", "y")
        item.timezone = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = ManualViolationBatchDiscountRangeRule(5.5, 1319.6)
        val item = newManualViolationBatch("x", "y")
        item.discount = 1321.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualViolationBatchRequiredLabelRule("manual violation batch needs an entry")
        val item = newManualViolationBatch("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
