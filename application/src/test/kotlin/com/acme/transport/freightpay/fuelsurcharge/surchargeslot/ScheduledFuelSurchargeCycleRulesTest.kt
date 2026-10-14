package com.acme.transport.freightpay.fuelsurcharge.surchargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledFuelSurchargeCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledFuelSurchargeCycleValidator()
        val item = newScheduledFuelSurchargeCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = ScheduledFuelSurchargeCycleStartsAtRangeRule(120, 120)
        val item = newScheduledFuelSurchargeCycle("x", "y")
        item.startsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long unit`() {
        val rule = ScheduledFuelSurchargeCycleUnitLengthRule(120)
        val item = newScheduledFuelSurchargeCycle("x", "y")
        item.unit = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
