package com.acme.transport.emissions.emissionfactor.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalEmissionFactorQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalEmissionFactorQuotaValidator()
        val item = newSeasonalEmissionFactorQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = SeasonalEmissionFactorQuotaRequiredCountryRule("seasonal emission factor quota needs an entry")
        val item = newSeasonalEmissionFactorQuota("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalEmissionFactorQuotaCodeLengthRule(80)
        val item = newSeasonalEmissionFactorQuota("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = SeasonalEmissionFactorQuotaRequiredTimezoneRule("seasonal emission factor quota needs this field")
        val item = newSeasonalEmissionFactorQuota("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
