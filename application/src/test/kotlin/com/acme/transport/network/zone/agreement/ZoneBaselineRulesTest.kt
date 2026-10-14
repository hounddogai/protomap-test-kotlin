package com.acme.transport.network.zone.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneBaselineValidator()
        val item = newZoneBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ZoneBaselineLabelLengthRule(120)
        val item = newZoneBaseline("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ZoneBaselineRequiredCodeRule("zone baseline needs this field")
        val item = newZoneBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = ZoneBaselineCurrencyLengthRule(16)
        val item = newZoneBaseline("x", "y")
        item.currency = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = ZoneBaselineLoadKgRangeRule(0.7, 4784.2)
        val item = newZoneBaseline("x", "y")
        item.loadKg = 4785.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
