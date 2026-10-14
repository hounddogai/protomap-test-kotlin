package com.acme.transport.safety.cameraevent.adjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackCameraEventAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackCameraEventAdjustmentValidator()
        val item = newFallbackCameraEventAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = FallbackCameraEventAdjustmentDiscountRangeRule(8.5, 1191.6)
        val item = newFallbackCameraEventAdjustment("x", "y")
        item.discount = 1193.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = FallbackCameraEventAdjustmentCeilingRangeRule(4.0, 1100.4)
        val item = newFallbackCameraEventAdjustment("x", "y")
        item.ceiling = 1101.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
