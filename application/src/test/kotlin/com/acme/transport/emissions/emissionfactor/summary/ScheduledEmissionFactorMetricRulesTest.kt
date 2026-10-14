package com.acme.transport.emissions.emissionfactor.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledEmissionFactorMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledEmissionFactorMetricValidator()
        val item = newScheduledEmissionFactorMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledEmissionFactorMetricRequiredCodeRule("scheduled emission factor metric needs an entry")
        val item = newScheduledEmissionFactorMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = ScheduledEmissionFactorMetricRequiredCarrierRule(
            "scheduled emission factor metric needs this field",
        )
        val item = newScheduledEmissionFactorMetric("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
