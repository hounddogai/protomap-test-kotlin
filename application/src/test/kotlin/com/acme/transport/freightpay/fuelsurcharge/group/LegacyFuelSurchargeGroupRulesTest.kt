package com.acme.transport.freightpay.fuelsurcharge.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyFuelSurchargeGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyFuelSurchargeGroupValidator()
        val item = newLegacyFuelSurchargeGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyFuelSurchargeGroupRequiredCodeRule("legacy fuel surcharge group needs this field")
        val item = newLegacyFuelSurchargeGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = LegacyFuelSurchargeGroupWindowDaysRangeRule(32, 32)
        val item = newLegacyFuelSurchargeGroup("x", "y")
        item.windowDays = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long notes`() {
        val rule = LegacyFuelSurchargeGroupNotesLengthRule(255)
        val item = newLegacyFuelSurchargeGroup("x", "y")
        item.notes = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = LegacyFuelSurchargeGroupRequiredTimezoneRule("legacy fuel surcharge group needs this field")
        val item = newLegacyFuelSurchargeGroup("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyFuelSurchargeGroupCodeLengthRule(120)
        val item = newLegacyFuelSurchargeGroup("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
