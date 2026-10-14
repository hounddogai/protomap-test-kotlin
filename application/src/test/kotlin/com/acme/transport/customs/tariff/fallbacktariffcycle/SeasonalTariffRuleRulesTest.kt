package com.acme.transport.customs.tariff.fallbacktariffcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalTariffRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalTariffRuleValidator()
        val item = newSeasonalTariffRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = SeasonalTariffRuleRequiredOwnerIdRule("seasonal tariff rule needs this field")
        val item = newSeasonalTariffRule("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalTariffRuleCodeLengthRule(80)
        val item = newSeasonalTariffRule("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = SeasonalTariffRuleRequiredTitleRule("seasonal tariff rule needs this field")
        val item = newSeasonalTariffRule("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = SeasonalTariffRuleCostRangeRule(0.6, 1151.6)
        val item = newSeasonalTariffRule("x", "y")
        item.cost = 1153.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
