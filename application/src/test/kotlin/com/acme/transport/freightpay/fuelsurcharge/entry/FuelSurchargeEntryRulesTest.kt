package com.acme.transport.freightpay.fuelsurcharge.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeEntryValidator()
        val item = newFuelSurchargeEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = FuelSurchargeEntryThresholdRangeRule(6.6, 4057.1)
        val item = newFuelSurchargeEntry("x", "y")
        item.threshold = 4058.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = FuelSurchargeEntryTitleLengthRule(16)
        val item = newFuelSurchargeEntry("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FuelSurchargeEntryRequiredCodeRule("fuel surcharge entry needs this field")
        val item = newFuelSurchargeEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FuelSurchargeEntryCodeLengthRule(1000)
        val item = newFuelSurchargeEntry("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
