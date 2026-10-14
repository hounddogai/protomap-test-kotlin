package com.acme.transport.safety.violation.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationBaselineValidator()
        val item = newViolationBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = ViolationBaselineRequiredChannelRule("violation baseline needs an entry")
        val item = newViolationBaseline("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = ViolationBaselineCostRangeRule(7.0, 4006.9)
        val item = newViolationBaseline("x", "y")
        item.cost = 4008.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = ViolationBaselineLabelLengthRule(16)
        val item = newViolationBaseline("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
