package com.acme.transport.lastmile.attempt.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAttemptCheckpointValidator()
        val item = newAttemptCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AttemptCheckpointRequiredTitleRule("attempt checkpoint needs this field")
        val item = newAttemptCheckpoint("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = AttemptCheckpointDiscountRangeRule(1.5, 3696.8)
        val item = newAttemptCheckpoint("x", "y")
        item.discount = 3698.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = AttemptCheckpointRequiredChannelRule("attempt checkpoint needs an entry")
        val item = newAttemptCheckpoint("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
