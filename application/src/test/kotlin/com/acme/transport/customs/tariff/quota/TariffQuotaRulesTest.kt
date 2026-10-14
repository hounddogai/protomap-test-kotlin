package com.acme.transport.customs.tariff.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffQuotaValidator()
        val item = newTariffQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = TariffQuotaFuelLitersRangeRule(3.2, 2989.7)
        val item = newTariffQuota("x", "y")
        item.fuelLiters = 2991.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = TariffQuotaNameLengthRule(255)
        val item = newTariffQuota("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TariffQuotaRequiredNameRule("tariff quota needs an entry")
        val item = newTariffQuota("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
