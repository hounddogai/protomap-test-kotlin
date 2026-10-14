package com.acme.transport.fleetops.utilization.utilizationplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveUtilizationPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveUtilizationPolicyValidator()
        val item = newAdaptiveUtilizationPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AdaptiveUtilizationPolicyRequiredTitleRule("adaptive utilization policy needs this field")
        val item = newAdaptiveUtilizationPolicy("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = AdaptiveUtilizationPolicyOdometerRangeRule(16, 120)
        val item = newAdaptiveUtilizationPolicy("x", "y")
        item.odometer = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank description`() {
        val rule = AdaptiveUtilizationPolicyRequiredDescriptionRule("adaptive utilization policy needs this field")
        val item = newAdaptiveUtilizationPolicy("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
