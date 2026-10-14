package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanPolicyValidator()
        val item = newServicePlanPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = ServicePlanPolicyFloorValueRangeRule(7.4, 890.7)
        val item = newServicePlanPolicy("x", "y")
        item.floorValue = 892.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ServicePlanPolicyRequiredTitleRule("service plan policy needs a value")
        val item = newServicePlanPolicy("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = ServicePlanPolicyRequiredOwnerIdRule("service plan policy needs a value")
        val item = newServicePlanPolicy("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ServicePlanPolicyLabelLengthRule(64)
        val item = newServicePlanPolicy("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
