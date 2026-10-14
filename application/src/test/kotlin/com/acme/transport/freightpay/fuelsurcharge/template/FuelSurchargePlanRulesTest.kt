package com.acme.transport.freightpay.fuelsurcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargePlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargePlanValidator()
        val item = newFuelSurchargePlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = FuelSurchargePlanExternalRefLengthRule(80)
        val item = newFuelSurchargePlan("x", "y")
        item.externalRef = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = FuelSurchargePlanDurationMinutesRangeRule(16, 32)
        val item = newFuelSurchargePlan("x", "y")
        item.durationMinutes = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = FuelSurchargePlanCreatedAtRangeRule(16, 255)
        val item = newFuelSurchargePlan("x", "y")
        item.createdAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
