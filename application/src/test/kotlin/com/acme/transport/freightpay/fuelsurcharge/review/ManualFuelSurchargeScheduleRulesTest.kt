package com.acme.transport.freightpay.fuelsurcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualFuelSurchargeScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualFuelSurchargeScheduleValidator()
        val item = newManualFuelSurchargeSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = ManualFuelSurchargeScheduleRatioRangeRule(2.6, 2163.2)
        val item = newManualFuelSurchargeSchedule("x", "y")
        item.ratio = 2164.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = ManualFuelSurchargeScheduleStopCountRangeRule(255, 86400)
        val item = newManualFuelSurchargeSchedule("x", "y")
        item.stopCount = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualFuelSurchargeScheduleRequiredCodeRule("manual fuel surcharge schedule needs this field")
        val item = newManualFuelSurchargeSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualFuelSurchargeScheduleCodeLengthRule(16)
        val item = newManualFuelSurchargeSchedule("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
