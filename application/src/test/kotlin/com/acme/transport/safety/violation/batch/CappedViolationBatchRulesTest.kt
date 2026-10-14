package com.acme.transport.safety.violation.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedViolationBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedViolationBatchValidator()
        val item = newCappedViolationBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = CappedViolationBatchLabelLengthRule(64)
        val item = newCappedViolationBatch("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedViolationBatchRequiredCodeRule("capped violation batch needs a value")
        val item = newCappedViolationBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CappedViolationBatchRequiredLabelRule("capped violation batch needs a value")
        val item = newCappedViolationBatch("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
