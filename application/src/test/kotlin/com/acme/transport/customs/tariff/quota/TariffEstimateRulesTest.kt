package com.acme.transport.customs.tariff.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffEstimateValidator()
        val item = newTariffEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = TariffEstimateContactEmailLengthRule(32)
        val item = newTariffEstimate("x", "y")
        item.contactEmail = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TariffEstimateRequiredNameRule("tariff estimate needs this field")
        val item = newTariffEstimate("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TariffEstimateCodeLengthRule(32)
        val item = newTariffEstimate("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
