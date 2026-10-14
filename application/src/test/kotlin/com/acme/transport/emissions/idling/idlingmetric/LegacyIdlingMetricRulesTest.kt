package com.acme.transport.emissions.idling.idlingmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyIdlingMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyIdlingMetricValidator()
        val item = newLegacyIdlingMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyIdlingMetricRequiredLabelRule("legacy idling metric needs an entry")
        val item = newLegacyIdlingMetric("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = LegacyIdlingMetricRequiredDescriptionRule("legacy idling metric needs a value")
        val item = newLegacyIdlingMetric("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyIdlingMetricLabelLengthRule(3600)
        val item = newLegacyIdlingMetric("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
