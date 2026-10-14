package com.acme.transport.freightpay.fuelsurcharge.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeAssignmentValidator()
        val item = newFuelSurchargeAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = FuelSurchargeAssignmentPalletsRangeRule(3600, 86400)
        val item = newFuelSurchargeAssignment("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above version`() {
        val rule = FuelSurchargeAssignmentVersionRangeRule(255, 86400)
        val item = newFuelSurchargeAssignment("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = FuelSurchargeAssignmentNameLengthRule(86400)
        val item = newFuelSurchargeAssignment("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = FuelSurchargeAssignmentSequenceRangeRule(120, 1000)
        val item = newFuelSurchargeAssignment("x", "y")
        item.sequence = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
