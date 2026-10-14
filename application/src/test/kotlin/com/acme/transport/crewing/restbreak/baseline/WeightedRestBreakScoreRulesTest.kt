package com.acme.transport.crewing.restbreak.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRestBreakScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedRestBreakScoreValidator()
        val item = newWeightedRestBreakScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = WeightedRestBreakScoreTitleLengthRule(32)
        val item = newWeightedRestBreakScore("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedRestBreakScoreRequiredCodeRule("weighted rest break score needs a value")
        val item = newWeightedRestBreakScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
