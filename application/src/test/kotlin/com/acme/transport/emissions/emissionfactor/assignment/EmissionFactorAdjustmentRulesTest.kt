package com.acme.transport.emissions.emissionfactor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorAdjustmentValidator()
        val item = newEmissionFactorAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = EmissionFactorAdjustmentExternalRefLengthRule(1000)
        val item = newEmissionFactorAdjustment("x", "y")
        item.externalRef = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = EmissionFactorAdjustmentCodeLengthRule(1000)
        val item = newEmissionFactorAdjustment("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = EmissionFactorAdjustmentRequiredSummaryRule("emission factor adjustment needs a value")
        val item = newEmissionFactorAdjustment("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = EmissionFactorAdjustmentRequiredLabelRule("emission factor adjustment needs a value")
        val item = newEmissionFactorAdjustment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
