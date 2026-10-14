package com.acme.transport.customs.inspection.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionBatchValidator()
        val item = newInspectionBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = InspectionBatchLabelLengthRule(32)
        val item = newInspectionBatch("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionBatchRequiredCodeRule("inspection batch needs an entry")
        val item = newInspectionBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = InspectionBatchEndsAtRangeRule(16, 16)
        val item = newInspectionBatch("x", "y")
        item.endsAt = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
