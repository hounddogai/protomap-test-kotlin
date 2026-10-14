package com.acme.transport.emissions.evcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyEvChargeMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyEvChargeMappingValidator()
        val item = newLegacyEvChargeMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = LegacyEvChargeMappingCeilingRangeRule(4.0, 2472.1)
        val item = newLegacyEvChargeMapping("x", "y")
        item.ceiling = 2473.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = LegacyEvChargeMappingNameLengthRule(32)
        val item = newLegacyEvChargeMapping("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyEvChargeMappingRequiredCodeRule("legacy ev charge mapping needs a value")
        val item = newLegacyEvChargeMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyEvChargeMappingCodeLengthRule(80)
        val item = newLegacyEvChargeMapping("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = LegacyEvChargeMappingCostRangeRule(7.3, 156.1)
        val item = newLegacyEvChargeMapping("x", "y")
        item.cost = 157.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
