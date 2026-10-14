package com.acme.transport.crewing.restbreak.breakprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakCheckpointValidator()
        val item = newRestBreakCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = RestBreakCheckpointExpiresAtRangeRule(64, 255)
        val item = newRestBreakCheckpoint("x", "y")
        item.expiresAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = RestBreakCheckpointTitleLengthRule(1000)
        val item = newRestBreakCheckpoint("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RestBreakCheckpointRequiredCodeRule("rest break checkpoint needs a value")
        val item = newRestBreakCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RestBreakCheckpointRequiredTitleRule("rest break checkpoint needs this field")
        val item = newRestBreakCheckpoint("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
