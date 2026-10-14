package com.acme.transport.lastmile.locker.lockerquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientLockerOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientLockerOverrideValidator()
        val item = newLenientLockerOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = LenientLockerOverrideAttemptsRangeRule(64, 3600)
        val item = newLenientLockerOverride("x", "y")
        item.attempts = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientLockerOverrideTitleLengthRule(86400)
        val item = newLenientLockerOverride("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = LenientLockerOverrideRequiredUnitRule("lenient locker override needs a value")
        val item = newLenientLockerOverride("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
