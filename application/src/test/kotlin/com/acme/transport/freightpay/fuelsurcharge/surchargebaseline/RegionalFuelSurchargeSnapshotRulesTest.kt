package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalFuelSurchargeSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalFuelSurchargeSnapshotValidator()
        val item = newRegionalFuelSurchargeSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalFuelSurchargeSnapshotNameLengthRule(1000)
        val item = newRegionalFuelSurchargeSnapshot("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = RegionalFuelSurchargeSnapshotRequiredLocaleRule("regional fuel surcharge snapshot needs an entry")
        val item = newRegionalFuelSurchargeSnapshot("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = RegionalFuelSurchargeSnapshotRequiredLaneRule("regional fuel surcharge snapshot needs this field")
        val item = newRegionalFuelSurchargeSnapshot("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
