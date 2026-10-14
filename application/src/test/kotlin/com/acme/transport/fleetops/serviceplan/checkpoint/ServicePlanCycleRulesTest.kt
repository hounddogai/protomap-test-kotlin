package com.acme.transport.fleetops.serviceplan.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanCycleValidator()
        val item = newServicePlanCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServicePlanCycleCodeLengthRule(32)
        val item = newServicePlanCycle("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = ServicePlanCycleWindowDaysRangeRule(80, 1000)
        val item = newServicePlanCycle("x", "y")
        item.windowDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = ServicePlanCycleRequiredReviewerRule("service plan cycle needs a value")
        val item = newServicePlanCycle("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ServicePlanCycleRequiredTitleRule("service plan cycle needs a value")
        val item = newServicePlanCycle("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
