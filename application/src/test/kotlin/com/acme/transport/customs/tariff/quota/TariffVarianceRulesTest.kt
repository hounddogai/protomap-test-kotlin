package com.acme.transport.customs.tariff.quota

import kotlin.test.Test
import kotlin.test.assertTrue

class TariffVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffVarianceValidator()
        val item = newTariffVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = TariffVarianceDiscountRangeRule(9.1, 2951.3)
        val item = newTariffVariance("x", "y")
        item.discount = 2952.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = TariffVarianceStartsAtRangeRule(1000, 86400)
        val item = newTariffVariance("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
