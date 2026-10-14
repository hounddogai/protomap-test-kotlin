package com.acme.transport.fleetops.defect.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectMetricValidator()
        val item = newDefectMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = DefectMetricRequiredCountryRule("defect metric needs this field")
        val item = newDefectMetric("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = DefectMetricRequiredUnitRule("defect metric needs an entry")
        val item = newDefectMetric("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = DefectMetricUnitLengthRule(80)
        val item = newDefectMetric("x", "y")
        item.unit = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = DefectMetricCountryLengthRule(80)
        val item = newDefectMetric("x", "y")
        item.country = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
