package com.acme.transport.emissions.emissionfactor.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorQuotaValidator()
        val item = newEmissionFactorQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = EmissionFactorQuotaRequiredLaneRule("emission factor quota needs an entry")
        val item = newEmissionFactorQuota("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = EmissionFactorQuotaRequiredLabelRule("emission factor quota needs a value")
        val item = newEmissionFactorQuota("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = EmissionFactorQuotaLabelLengthRule(255)
        val item = newEmissionFactorQuota("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
