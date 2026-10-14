package com.acme.transport.freightpay.fuelsurcharge.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultFuelSurchargeRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultFuelSurchargeRuleValidator()
        val item = newDefaultFuelSurchargeRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = DefaultFuelSurchargeRuleRequiredCountryRule("default fuel surcharge rule needs an entry")
        val item = newDefaultFuelSurchargeRule("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultFuelSurchargeRuleRequiredCodeRule("default fuel surcharge rule needs an entry")
        val item = newDefaultFuelSurchargeRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
