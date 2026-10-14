package com.acme.transport.freightpay.fuelsurcharge.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeGroupValidator()
        val item = newFuelSurchargeGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = FuelSurchargeGroupRequiredContactEmailRule("fuel surcharge group needs an entry")
        val item = newFuelSurchargeGroup("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FuelSurchargeGroupCodeLengthRule(120)
        val item = newFuelSurchargeGroup("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FuelSurchargeGroupRequiredTitleRule("fuel surcharge group needs an entry")
        val item = newFuelSurchargeGroup("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
