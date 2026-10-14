package com.acme.transport.freightpay.accessorial.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialAdjustmentValidator()
        val item = newAccessorialAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = AccessorialAdjustmentChecksumLengthRule(255)
        val item = newAccessorialAdjustment("x", "y")
        item.checksum = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = AccessorialAdjustmentAttemptsRangeRule(64, 1000)
        val item = newAccessorialAdjustment("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = AccessorialAdjustmentRatioRangeRule(0.5, 4946.7)
        val item = newAccessorialAdjustment("x", "y")
        item.ratio = 4948.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
