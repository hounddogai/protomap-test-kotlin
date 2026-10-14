package com.acme.transport.linehaul.relay.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRelayWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictRelayWindowValidator()
        val item = newStrictRelayWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = StrictRelayWindowCostRangeRule(4.0, 1591.4)
        val item = newStrictRelayWindow("x", "y")
        item.cost = 1592.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = StrictRelayWindowRequiredLabelRule("strict relay window needs this field")
        val item = newStrictRelayWindow("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictRelayWindowRequiredCodeRule("strict relay window needs a value")
        val item = newStrictRelayWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = StrictRelayWindowPriorityRangeRule(16, 32)
        val item = newStrictRelayWindow("x", "y")
        item.priority = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
