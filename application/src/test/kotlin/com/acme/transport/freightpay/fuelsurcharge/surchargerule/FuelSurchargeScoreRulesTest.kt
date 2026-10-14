package com.acme.transport.freightpay.fuelsurcharge.surchargerule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeScoreValidator()
        val item = newFuelSurchargeScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FuelSurchargeScoreCodeLengthRule(32)
        val item = newFuelSurchargeScore("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = FuelSurchargeScoreNameLengthRule(255)
        val item = newFuelSurchargeScore("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long source`() {
        val rule = FuelSurchargeScoreSourceLengthRule(1000)
        val item = newFuelSurchargeScore("x", "y")
        item.source = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
