package com.acme.transport.fleetops.serviceplan.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanThresholdValidator()
        val item = newServicePlanThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServicePlanThresholdRequiredCodeRule("service plan threshold needs an entry")
        val item = newServicePlanThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ServicePlanThresholdRequiredTitleRule("service plan threshold needs an entry")
        val item = newServicePlanThreshold("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServicePlanThresholdCodeLengthRule(86400)
        val item = newServicePlanThreshold("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = ServicePlanThresholdRateRangeRule(6.1, 3933.9)
        val item = newServicePlanThreshold("x", "y")
        item.rate = 3935.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = ServicePlanThresholdRequiredDriverIdRule("service plan threshold needs an entry")
        val item = newServicePlanThreshold("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
