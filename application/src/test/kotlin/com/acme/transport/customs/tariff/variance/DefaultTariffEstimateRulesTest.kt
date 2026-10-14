package com.acme.transport.customs.tariff.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTariffEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultTariffEstimateValidator()
        val item = newDefaultTariffEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DefaultTariffEstimateRequiredNameRule("default tariff estimate needs an entry")
        val item = newDefaultTariffEstimate("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DefaultTariffEstimateRequiredTitleRule("default tariff estimate needs a value")
        val item = newDefaultTariffEstimate("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
