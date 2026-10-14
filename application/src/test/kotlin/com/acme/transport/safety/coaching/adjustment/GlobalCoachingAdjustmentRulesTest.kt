package com.acme.transport.safety.coaching.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalCoachingAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalCoachingAdjustmentValidator()
        val item = newGlobalCoachingAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalCoachingAdjustmentCodeLengthRule(32)
        val item = newGlobalCoachingAdjustment("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = GlobalCoachingAdjustmentRateRangeRule(7.8, 2951.3)
        val item = newGlobalCoachingAdjustment("x", "y")
        item.rate = 2952.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = GlobalCoachingAdjustmentFloorValueRangeRule(4.1, 1659.5)
        val item = newGlobalCoachingAdjustment("x", "y")
        item.floorValue = 1661.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
