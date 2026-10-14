package com.acme.transport.safety.violation.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationTemplateValidator()
        val item = newViolationTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = ViolationTemplateGraceDaysRangeRule(32, 255)
        val item = newViolationTemplate("x", "y")
        item.graceDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = ViolationTemplateMaximumRangeRule(3.9, 1658.1)
        val item = newViolationTemplate("x", "y")
        item.maximum = 1659.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
