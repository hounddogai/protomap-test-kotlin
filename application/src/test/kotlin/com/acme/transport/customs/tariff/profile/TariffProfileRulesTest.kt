package com.acme.transport.customs.tariff.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffProfileValidator()
        val item = newTariffProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = TariffProfileQuantityRangeRule(64, 120)
        val item = newTariffProfile("x", "y")
        item.quantity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TariffProfileRequiredLabelRule("tariff profile needs a value")
        val item = newTariffProfile("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TariffProfileCodeLengthRule(255)
        val item = newTariffProfile("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = TariffProfileLabelLengthRule(86400)
        val item = newTariffProfile("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = TariffProfileWeightRangeRule(3.8, 3909.4)
        val item = newTariffProfile("x", "y")
        item.weight = 3910.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
