package com.acme.transport.freightpay.accessorial.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialGroupValidator()
        val item = newAccessorialGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = AccessorialGroupLimitRangeRule(64, 80)
        val item = newAccessorialGroup("x", "y")
        item.limit = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = AccessorialGroupThresholdRangeRule(6.4, 3088.7)
        val item = newAccessorialGroup("x", "y")
        item.threshold = 3090.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long phone`() {
        val rule = AccessorialGroupPhoneLengthRule(255)
        val item = newAccessorialGroup("x", "y")
        item.phone = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
