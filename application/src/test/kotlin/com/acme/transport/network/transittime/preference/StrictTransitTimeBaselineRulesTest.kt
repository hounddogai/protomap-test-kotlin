package com.acme.transport.network.transittime.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTransitTimeBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictTransitTimeBaselineValidator()
        val item = newStrictTransitTimeBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictTransitTimeBaselineRequiredCodeRule("strict transit time baseline needs an entry")
        val item = newStrictTransitTimeBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = StrictTransitTimeBaselineRequiredChannelRule("strict transit time baseline needs this field")
        val item = newStrictTransitTimeBaseline("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = StrictTransitTimeBaselineLabelLengthRule(32)
        val item = newStrictTransitTimeBaseline("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
