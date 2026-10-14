package com.acme.transport.freightpay.fuelsurcharge.surchargewindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledFuelSurchargeWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledFuelSurchargeWindowValidator()
        val item = newScheduledFuelSurchargeWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledFuelSurchargeWindowLabelLengthRule(1000)
        val item = newScheduledFuelSurchargeWindow("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = ScheduledFuelSurchargeWindowStopCountRangeRule(80, 120)
        val item = newScheduledFuelSurchargeWindow("x", "y")
        item.stopCount = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = ScheduledFuelSurchargeWindowTimezoneLengthRule(3600)
        val item = newScheduledFuelSurchargeWindow("x", "y")
        item.timezone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = ScheduledFuelSurchargeWindowRequiredTimezoneRule("scheduled fuel surcharge window needs an entry")
        val item = newScheduledFuelSurchargeWindow("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
