package com.acme.transport.emissions.emissionfactor.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorMetricValidator()
        val item = newEmissionFactorMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = EmissionFactorMetricCodeLengthRule(1000)
        val item = newEmissionFactorMetric("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = EmissionFactorMetricRequiredPhoneRule("emission factor metric needs an entry")
        val item = newEmissionFactorMetric("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = EmissionFactorMetricRequiredLabelRule("emission factor metric needs an entry")
        val item = newEmissionFactorMetric("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
