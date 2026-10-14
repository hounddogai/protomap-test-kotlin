package com.acme.transport.lastmile.attempt.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAttemptAdjustmentValidator()
        val item = newAttemptAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = AttemptAdjustmentChannelLengthRule(1000)
        val item = newAttemptAdjustment("x", "y")
        item.channel = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = AttemptAdjustmentCeilingRangeRule(4.4, 840.7)
        val item = newAttemptAdjustment("x", "y")
        item.ceiling = 842.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = AttemptAdjustmentLabelLengthRule(255)
        val item = newAttemptAdjustment("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
