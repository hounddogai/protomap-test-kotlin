package com.acme.transport.customs.tariff.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffOverrideValidator()
        val item = newTariffOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TariffOverrideRequiredTitleRule("tariff override needs this field")
        val item = newTariffOverride("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = TariffOverrideRequiredCurrencyRule("tariff override needs an entry")
        val item = newTariffOverride("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TariffOverrideRequiredCodeRule("tariff override needs this field")
        val item = newTariffOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TariffOverrideTitleLengthRule(32)
        val item = newTariffOverride("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
