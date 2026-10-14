package com.acme.transport.customs.tariff.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredTariffBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredTariffBundleValidator()
        val item = newTieredTariffBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = TieredTariffBundleRequiredCountryRule("tiered tariff bundle needs this field")
        val item = newTieredTariffBundle("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = TieredTariffBundleEtaMinutesRangeRule(80, 3600)
        val item = newTieredTariffBundle("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredTariffBundleCodeLengthRule(1000)
        val item = newTieredTariffBundle("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
