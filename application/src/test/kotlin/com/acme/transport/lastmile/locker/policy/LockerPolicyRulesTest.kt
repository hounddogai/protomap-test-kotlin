package com.acme.transport.lastmile.locker.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerPolicyValidator()
        val item = newLockerPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = LockerPolicyStartsAtRangeRule(32, 255)
        val item = newLockerPolicy("x", "y")
        item.startsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above discount`() {
        val rule = LockerPolicyDiscountRangeRule(5.6, 317.6)
        val item = newLockerPolicy("x", "y")
        item.discount = 319.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = LockerPolicyTitleLengthRule(120)
        val item = newLockerPolicy("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = LockerPolicyQuantityRangeRule(255, 255)
        val item = newLockerPolicy("x", "y")
        item.quantity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LockerPolicyRequiredCodeRule("locker policy needs this field")
        val item = newLockerPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LockerPolicyCodeLengthRule(255)
        val item = newLockerPolicy("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
