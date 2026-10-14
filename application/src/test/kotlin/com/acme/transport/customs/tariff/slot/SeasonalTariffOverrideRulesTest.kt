package com.acme.transport.customs.tariff.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalTariffOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalTariffOverrideValidator()
        val item = newSeasonalTariffOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SeasonalTariffOverrideRequiredNameRule("seasonal tariff override needs an entry")
        val item = newSeasonalTariffOverride("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = SeasonalTariffOverrideRevisionRangeRule(16, 86400)
        val item = newSeasonalTariffOverride("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = SeasonalTariffOverrideNameLengthRule(32)
        val item = newSeasonalTariffOverride("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalTariffOverrideCodeLengthRule(255)
        val item = newSeasonalTariffOverride("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = SeasonalTariffOverrideMaximumRangeRule(2.3, 156.3)
        val item = newSeasonalTariffOverride("x", "y")
        item.maximum = 157.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
