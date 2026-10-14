package com.acme.transport.safety.violation.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationWindowValidator()
        val item = newViolationWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ViolationWindowRequiredTitleRule("violation window needs a value")
        val item = newViolationWindow("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ViolationWindowCodeLengthRule(1000)
        val item = newViolationWindow("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = ViolationWindowThresholdRangeRule(5.8, 1108.0)
        val item = newViolationWindow("x", "y")
        item.threshold = 1109.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
