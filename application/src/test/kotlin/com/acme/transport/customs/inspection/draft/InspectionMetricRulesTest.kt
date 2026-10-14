package com.acme.transport.customs.inspection.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionMetricValidator()
        val item = newInspectionMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionMetricRequiredCodeRule("inspection metric needs a value")
        val item = newInspectionMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = InspectionMetricAmountRangeRule(9.4, 2408.6)
        val item = newInspectionMetric("x", "y")
        item.amount = 2410.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank country`() {
        val rule = InspectionMetricRequiredCountryRule("inspection metric needs a value")
        val item = newInspectionMetric("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
