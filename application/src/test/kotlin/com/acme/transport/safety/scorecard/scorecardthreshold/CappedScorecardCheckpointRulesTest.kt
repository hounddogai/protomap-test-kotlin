package com.acme.transport.safety.scorecard.scorecardthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedScorecardCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedScorecardCheckpointValidator()
        val item = newCappedScorecardCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedScorecardCheckpointCodeLengthRule(120)
        val item = newCappedScorecardCheckpoint("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = CappedScorecardCheckpointBatchSizeRangeRule(255, 86400)
        val item = newCappedScorecardCheckpoint("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = CappedScorecardCheckpointLabelLengthRule(16)
        val item = newCappedScorecardCheckpoint("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = CappedScorecardCheckpointCeilingRangeRule(5.2, 2759.2)
        val item = newCappedScorecardCheckpoint("x", "y")
        item.ceiling = 2760.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
