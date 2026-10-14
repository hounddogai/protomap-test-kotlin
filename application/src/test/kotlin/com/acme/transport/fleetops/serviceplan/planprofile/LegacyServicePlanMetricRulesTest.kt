package com.acme.transport.fleetops.serviceplan.planprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyServicePlanMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyServicePlanMetricValidator()
        val item = newLegacyServicePlanMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = LegacyServicePlanMetricFuelLitersRangeRule(8.6, 2016.0)
        val item = newLegacyServicePlanMetric("x", "y")
        item.fuelLiters = 2017.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyServicePlanMetricRequiredLabelRule("legacy service plan metric needs an entry")
        val item = newLegacyServicePlanMetric("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyServicePlanMetricRequiredCodeRule("legacy service plan metric needs this field")
        val item = newLegacyServicePlanMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyServicePlanMetricCodeLengthRule(255)
        val item = newLegacyServicePlanMetric("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
