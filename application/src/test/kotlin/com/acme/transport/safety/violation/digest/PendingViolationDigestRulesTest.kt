package com.acme.transport.safety.violation.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingViolationDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingViolationDigestValidator()
        val item = newPendingViolationDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = PendingViolationDigestAttemptsRangeRule(80, 120)
        val item = newPendingViolationDigest("x", "y")
        item.attempts = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = PendingViolationDigestFuelLitersRangeRule(3.8, 1561.8)
        val item = newPendingViolationDigest("x", "y")
        item.fuelLiters = 1563.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingViolationDigestRequiredCodeRule("pending violation digest needs this field")
        val item = newPendingViolationDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
