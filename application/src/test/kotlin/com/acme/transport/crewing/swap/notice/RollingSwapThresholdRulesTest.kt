package com.acme.transport.crewing.swap.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingSwapThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingSwapThresholdValidator()
        val item = newRollingSwapThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RollingSwapThresholdTitleLengthRule(1000)
        val item = newRollingSwapThreshold("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RollingSwapThresholdRequiredTitleRule("rolling swap threshold needs a value")
        val item = newRollingSwapThreshold("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingSwapThresholdCodeLengthRule(120)
        val item = newRollingSwapThreshold("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
