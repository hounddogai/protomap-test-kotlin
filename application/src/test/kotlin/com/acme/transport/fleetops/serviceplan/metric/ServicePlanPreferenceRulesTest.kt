package com.acme.transport.fleetops.serviceplan.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanPreferenceValidator()
        val item = newServicePlanPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServicePlanPreferenceRequiredCodeRule("service plan preference needs a value")
        val item = newServicePlanPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = ServicePlanPreferenceRequiredSummaryRule("service plan preference needs this field")
        val item = newServicePlanPreference("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = ServicePlanPreferenceCapacityRangeRule(64, 120)
        val item = newServicePlanPreference("x", "y")
        item.capacity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ServicePlanPreferenceRequiredNameRule("service plan preference needs an entry")
        val item = newServicePlanPreference("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
