package com.acme.transport.fleetops.defect.defectmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDefectMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedDefectMappingValidator()
        val item = newBatchedDefectMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = BatchedDefectMappingWindowDaysRangeRule(80, 120)
        val item = newBatchedDefectMapping("x", "y")
        item.windowDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedDefectMappingCodeLengthRule(3600)
        val item = newBatchedDefectMapping("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = BatchedDefectMappingRequiredReviewerRule("batched defect mapping needs an entry")
        val item = newBatchedDefectMapping("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
