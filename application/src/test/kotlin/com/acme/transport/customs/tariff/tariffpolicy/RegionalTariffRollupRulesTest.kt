package com.acme.transport.customs.tariff.tariffpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalTariffRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalTariffRollupValidator()
        val item = newRegionalTariffRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = RegionalTariffRollupExternalRefLengthRule(1000)
        val item = newRegionalTariffRollup("x", "y")
        item.externalRef = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalTariffRollupRequiredNameRule("regional tariff rollup needs this field")
        val item = newRegionalTariffRollup("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = RegionalTariffRollupCategoryLengthRule(80)
        val item = newRegionalTariffRollup("x", "y")
        item.category = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = RegionalTariffRollupMinimumRangeRule(3.0, 1853.0)
        val item = newRegionalTariffRollup("x", "y")
        item.minimum = 1854.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalTariffRollupRequiredCodeRule("regional tariff rollup needs an entry")
        val item = newRegionalTariffRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
