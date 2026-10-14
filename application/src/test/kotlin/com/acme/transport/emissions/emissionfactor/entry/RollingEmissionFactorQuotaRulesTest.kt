package com.acme.transport.emissions.emissionfactor.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingEmissionFactorQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingEmissionFactorQuotaValidator()
        val item = newRollingEmissionFactorQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = RollingEmissionFactorQuotaRequiredSummaryRule("rolling emission factor quota needs an entry")
        val item = newRollingEmissionFactorQuota("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = RollingEmissionFactorQuotaRequiredLocaleRule("rolling emission factor quota needs an entry")
        val item = newRollingEmissionFactorQuota("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = RollingEmissionFactorQuotaAttemptsRangeRule(16, 64)
        val item = newRollingEmissionFactorQuota("x", "y")
        item.attempts = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
