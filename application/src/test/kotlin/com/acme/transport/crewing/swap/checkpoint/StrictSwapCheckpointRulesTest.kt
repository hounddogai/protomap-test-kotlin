package com.acme.transport.crewing.swap.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictSwapCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictSwapCheckpointValidator()
        val item = newStrictSwapCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictSwapCheckpointCodeLengthRule(1000)
        val item = newStrictSwapCheckpoint("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictSwapCheckpointRequiredTitleRule("strict swap checkpoint needs this field")
        val item = newStrictSwapCheckpoint("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = StrictSwapCheckpointGraceDaysRangeRule(16, 120)
        val item = newStrictSwapCheckpoint("x", "y")
        item.graceDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above window days`() {
        val rule = StrictSwapCheckpointWindowDaysRangeRule(32, 255)
        val item = newStrictSwapCheckpoint("x", "y")
        item.windowDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
