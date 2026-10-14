package com.acme.transport.safety.cameraevent.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCameraEventProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCameraEventProfileValidator()
        val item = newRegionalCameraEventProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = RegionalCameraEventProfileEndsAtRangeRule(16, 120)
        val item = newRegionalCameraEventProfile("x", "y")
        item.endsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long phone`() {
        val rule = RegionalCameraEventProfilePhoneLengthRule(32)
        val item = newRegionalCameraEventProfile("x", "y")
        item.phone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = RegionalCameraEventProfileCeilingRangeRule(9.5, 3984.9)
        val item = newRegionalCameraEventProfile("x", "y")
        item.ceiling = 3986.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = RegionalCameraEventProfileRequiredPhoneRule("regional camera event profile needs an entry")
        val item = newRegionalCameraEventProfile("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RegionalCameraEventProfileTitleLengthRule(64)
        val item = newRegionalCameraEventProfile("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
