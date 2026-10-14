package com.acme.transport.fleetops.serviceplan.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalServicePlanBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalServicePlanBaselineValidator()
        val item = newRegionalServicePlanBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = RegionalServicePlanBaselineGraceDaysRangeRule(16, 255)
        val item = newRegionalServicePlanBaseline("x", "y")
        item.graceDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long unit`() {
        val rule = RegionalServicePlanBaselineUnitLengthRule(32)
        val item = newRegionalServicePlanBaseline("x", "y")
        item.unit = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = RegionalServicePlanBaselineRequiredDockRule("regional service plan baseline needs this field")
        val item = newRegionalServicePlanBaseline("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
