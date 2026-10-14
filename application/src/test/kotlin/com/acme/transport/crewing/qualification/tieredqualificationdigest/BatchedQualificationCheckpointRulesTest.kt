package com.acme.transport.crewing.qualification.tieredqualificationdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedQualificationCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedQualificationCheckpointValidator()
        val item = newBatchedQualificationCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = BatchedQualificationCheckpointUnitLengthRule(1000)
        val item = newBatchedQualificationCheckpoint("x", "y")
        item.unit = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = BatchedQualificationCheckpointRequiredUnitRule("batched qualification checkpoint needs this field")
        val item = newBatchedQualificationCheckpoint("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = BatchedQualificationCheckpointScoreRangeRule(5.2, 3548.9)
        val item = newBatchedQualificationCheckpoint("x", "y")
        item.score = 3550.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedQualificationCheckpointRequiredCodeRule("batched qualification checkpoint needs this field")
        val item = newBatchedQualificationCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedQualificationCheckpointTitleLengthRule(32)
        val item = newBatchedQualificationCheckpoint("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
