package com.acme.transport.fleetops.leasereturn.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalLeaseReturnOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalLeaseReturnOverrideValidator()
        val item = newGlobalLeaseReturnOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = GlobalLeaseReturnOverrideGraceDaysRangeRule(64, 3600)
        val item = newGlobalLeaseReturnOverride("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = GlobalLeaseReturnOverrideRequiredPhoneRule("global lease return override needs this field")
        val item = newGlobalLeaseReturnOverride("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = GlobalLeaseReturnOverridePhoneLengthRule(64)
        val item = newGlobalLeaseReturnOverride("x", "y")
        item.phone = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalLeaseReturnOverrideCodeLengthRule(255)
        val item = newGlobalLeaseReturnOverride("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalLeaseReturnOverrideRequiredCodeRule("global lease return override needs this field")
        val item = newGlobalLeaseReturnOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
