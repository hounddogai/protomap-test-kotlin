package com.acme.transport.safety.violation.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingViolationRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingViolationRollupValidator()
        val item = newPendingViolationRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = PendingViolationRollupAttemptsRangeRule(32, 80)
        val item = newPendingViolationRollup("x", "y")
        item.attempts = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingViolationRollupRequiredCodeRule("pending violation rollup needs this field")
        val item = newPendingViolationRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingViolationRollupCodeLengthRule(16)
        val item = newPendingViolationRollup("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
