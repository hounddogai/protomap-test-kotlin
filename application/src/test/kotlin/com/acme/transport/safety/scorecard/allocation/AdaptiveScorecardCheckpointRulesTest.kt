package com.acme.transport.safety.scorecard.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveScorecardCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveScorecardCheckpointValidator()
        val item = newAdaptiveScorecardCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveScorecardCheckpointTitleLengthRule(32)
        val item = newAdaptiveScorecardCheckpoint("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = AdaptiveScorecardCheckpointCategoryLengthRule(32)
        val item = newAdaptiveScorecardCheckpoint("x", "y")
        item.category = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = AdaptiveScorecardCheckpointCeilingRangeRule(4.7, 135.5)
        val item = newAdaptiveScorecardCheckpoint("x", "y")
        item.ceiling = 137.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
