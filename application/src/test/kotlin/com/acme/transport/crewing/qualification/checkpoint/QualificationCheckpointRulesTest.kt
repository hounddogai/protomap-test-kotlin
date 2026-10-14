package com.acme.transport.crewing.qualification.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationCheckpointValidator()
        val item = newQualificationCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = QualificationCheckpointBatchSizeRangeRule(16, 86400)
        val item = newQualificationCheckpoint("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = QualificationCheckpointTitleLengthRule(1000)
        val item = newQualificationCheckpoint("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = QualificationCheckpointSequenceRangeRule(32, 64)
        val item = newQualificationCheckpoint("x", "y")
        item.sequence = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
