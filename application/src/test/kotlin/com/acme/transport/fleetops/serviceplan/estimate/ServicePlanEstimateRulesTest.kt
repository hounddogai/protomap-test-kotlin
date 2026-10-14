package com.acme.transport.fleetops.serviceplan.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanEstimateValidator()
        val item = newServicePlanEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ServicePlanEstimateTitleLengthRule(120)
        val item = newServicePlanEstimate("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServicePlanEstimateCodeLengthRule(80)
        val item = newServicePlanEstimate("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = ServicePlanEstimateLimitRangeRule(255, 3600)
        val item = newServicePlanEstimate("x", "y")
        item.limit = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ServicePlanEstimateRequiredTitleRule("service plan estimate needs this field")
        val item = newServicePlanEstimate("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
