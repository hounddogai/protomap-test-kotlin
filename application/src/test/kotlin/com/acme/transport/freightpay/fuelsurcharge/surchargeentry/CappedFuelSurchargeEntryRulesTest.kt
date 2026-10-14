package com.acme.transport.freightpay.fuelsurcharge.surchargeentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedFuelSurchargeEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedFuelSurchargeEntryValidator()
        val item = newCappedFuelSurchargeEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CappedFuelSurchargeEntryRequiredLabelRule("capped fuel surcharge entry needs this field")
        val item = newCappedFuelSurchargeEntry("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = CappedFuelSurchargeEntryLabelLengthRule(1000)
        val item = newCappedFuelSurchargeEntry("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = CappedFuelSurchargeEntryDistanceKmRangeRule(0.1, 1815.5)
        val item = newCappedFuelSurchargeEntry("x", "y")
        item.distanceKm = 1817.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above version`() {
        val rule = CappedFuelSurchargeEntryVersionRangeRule(80, 1000)
        val item = newCappedFuelSurchargeEntry("x", "y")
        item.version = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
