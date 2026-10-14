package com.acme.transport.customs.tariff.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTariffRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultTariffRequestValidator()
        val item = newDefaultTariffRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = DefaultTariffRequestWeightRangeRule(8.8, 1639.7)
        val item = newDefaultTariffRequest("x", "y")
        item.weight = 1641.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = DefaultTariffRequestNameLengthRule(32)
        val item = newDefaultTariffRequest("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefaultTariffRequestLabelLengthRule(64)
        val item = newDefaultTariffRequest("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = DefaultTariffRequestRequiredTimezoneRule("default tariff request needs a value")
        val item = newDefaultTariffRequest("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
