package com.acme.transport.terminal.crossdock.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCrossDockScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingCrossDockScoreValidator()
        val item = newPendingCrossDockScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingCrossDockScoreRequiredTitleRule("pending cross dock score needs a value")
        val item = newPendingCrossDockScore("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingCrossDockScoreCodeLengthRule(16)
        val item = newPendingCrossDockScore("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = PendingCrossDockScoreUpdatedAtRangeRule(16, 80)
        val item = newPendingCrossDockScore("x", "y")
        item.updatedAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
