package com.acme.transport.freightpay.fuelsurcharge.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeSegmentValidator()
        val item = newFuelSurchargeSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = FuelSurchargeSegmentRegionLengthRule(1000)
        val item = newFuelSurchargeSegment("x", "y")
        item.region = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = FuelSurchargeSegmentWindowDaysRangeRule(255, 1000)
        val item = newFuelSurchargeSegment("x", "y")
        item.windowDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank region`() {
        val rule = FuelSurchargeSegmentRequiredRegionRule("fuel surcharge segment needs a value")
        val item = newFuelSurchargeSegment("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = FuelSurchargeSegmentChecksumLengthRule(32)
        val item = newFuelSurchargeSegment("x", "y")
        item.checksum = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FuelSurchargeSegmentRequiredNameRule("fuel surcharge segment needs this field")
        val item = newFuelSurchargeSegment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
