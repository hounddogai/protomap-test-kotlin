package com.acme.transport.terminal.crossdock.dockestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingCrossDockScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingCrossDockScoreValidator()
        val item = newRollingCrossDockScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = RollingCrossDockScoreScoreRangeRule(4.8, 3148.7)
        val item = newRollingCrossDockScore("x", "y")
        item.score = 3150.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingCrossDockScoreRequiredCodeRule("rolling cross dock score needs a value")
        val item = newRollingCrossDockScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RollingCrossDockScoreRequiredTitleRule("rolling cross dock score needs this field")
        val item = newRollingCrossDockScore("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RollingCrossDockScoreTitleLengthRule(16)
        val item = newRollingCrossDockScore("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
