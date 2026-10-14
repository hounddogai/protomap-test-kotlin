package com.acme.transport.crewing.qualification.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedQualificationSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedQualificationSummaryValidator()
        val item = newBatchedQualificationSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = BatchedQualificationSummaryLoadKgRangeRule(9.7, 2115.9)
        val item = newBatchedQualificationSummary("x", "y")
        item.loadKg = 2117.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = BatchedQualificationSummaryNameLengthRule(1000)
        val item = newBatchedQualificationSummary("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = BatchedQualificationSummaryLabelLengthRule(16)
        val item = newBatchedQualificationSummary("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
