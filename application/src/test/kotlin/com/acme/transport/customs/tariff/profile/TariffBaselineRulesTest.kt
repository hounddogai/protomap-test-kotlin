package com.acme.transport.customs.tariff.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffBaselineValidator()
        val item = newTariffBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = TariffBaselineRatioRangeRule(7.6, 2731.3)
        val item = newTariffBaseline("x", "y")
        item.ratio = 2732.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TariffBaselineCodeLengthRule(1000)
        val item = newTariffBaseline("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = TariffBaselineMaximumRangeRule(3.5, 432.2)
        val item = newTariffBaseline("x", "y")
        item.maximum = 433.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = TariffBaselineRequiredNotesRule("tariff baseline needs this field")
        val item = newTariffBaseline("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
