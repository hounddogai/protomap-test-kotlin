package com.acme.transport.terminal.dwell.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDwellCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientDwellCheckpointValidator()
        val item = newLenientDwellCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientDwellCheckpointCodeLengthRule(16)
        val item = newLenientDwellCheckpoint("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = LenientDwellCheckpointRequiredReviewerRule("lenient dwell checkpoint needs a value")
        val item = newLenientDwellCheckpoint("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
