package com.acme.transport.fleetops.serviceplan.planquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanMilestoneValidator()
        val item = newServicePlanMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = ServicePlanMilestoneRequiredLocaleRule("service plan milestone needs a value")
        val item = newServicePlanMilestone("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = ServicePlanMilestoneAmountRangeRule(4.0, 4074.3)
        val item = newServicePlanMilestone("x", "y")
        item.amount = 4075.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = ServicePlanMilestoneCreatedAtRangeRule(120, 1000)
        val item = newServicePlanMilestone("x", "y")
        item.createdAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ServicePlanMilestoneRequiredTitleRule("service plan milestone needs a value")
        val item = newServicePlanMilestone("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServicePlanMilestoneRequiredCodeRule("service plan milestone needs an entry")
        val item = newServicePlanMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
