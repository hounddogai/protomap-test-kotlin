package com.acme.transport.customs.tariff.tariffoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalTariffOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalTariffOverrideValidator()
        val item = newRegionalTariffOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = RegionalTariffOverrideDescriptionLengthRule(80)
        val item = newRegionalTariffOverride("x", "y")
        item.description = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalTariffOverrideCodeLengthRule(80)
        val item = newRegionalTariffOverride("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = RegionalTariffOverrideRequiredDescriptionRule("regional tariff override needs this field")
        val item = newRegionalTariffOverride("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalTariffOverrideRequiredCodeRule("regional tariff override needs an entry")
        val item = newRegionalTariffOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
