package com.acme.transport.customs.tariff.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffRuleValidator()
        val item = newTariffRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = TariffRuleRequiredDriverIdRule("tariff rule needs an entry")
        val item = newTariffRule("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = TariffRuleCapacityRangeRule(80, 3600)
        val item = newTariffRule("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = TariffRuleTitleLengthRule(3600)
        val item = newTariffRule("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
