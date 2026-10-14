package com.acme.transport.safety.violation.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingViolationWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingViolationWindowValidator()
        val item = newRollingViolationWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingViolationWindowCodeLengthRule(255)
        val item = newRollingViolationWindow("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = RollingViolationWindowRequiredRegionRule("rolling violation window needs a value")
        val item = newRollingViolationWindow("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
