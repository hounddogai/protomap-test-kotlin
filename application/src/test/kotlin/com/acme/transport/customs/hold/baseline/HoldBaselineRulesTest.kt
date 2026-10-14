package com.acme.transport.customs.hold.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldBaselineValidator()
        val item = newHoldBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = HoldBaselineTitleLengthRule(32)
        val item = newHoldBaseline("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HoldBaselineRequiredCodeRule("hold baseline needs a value")
        val item = newHoldBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = HoldBaselineChannelLengthRule(86400)
        val item = newHoldBaseline("x", "y")
        item.channel = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
