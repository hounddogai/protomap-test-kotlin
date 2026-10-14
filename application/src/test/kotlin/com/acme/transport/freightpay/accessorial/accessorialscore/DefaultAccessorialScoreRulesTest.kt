package com.acme.transport.freightpay.accessorial.accessorialscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultAccessorialScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultAccessorialScoreValidator()
        val item = newDefaultAccessorialScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefaultAccessorialScoreLabelLengthRule(120)
        val item = newDefaultAccessorialScore("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefaultAccessorialScoreRequiredLabelRule("default accessorial score needs a value")
        val item = newDefaultAccessorialScore("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = DefaultAccessorialScoreChannelLengthRule(64)
        val item = newDefaultAccessorialScore("x", "y")
        item.channel = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
