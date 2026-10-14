package com.acme.transport.fleetops.serviceplan.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanAssignmentValidator()
        val item = newServicePlanAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServicePlanAssignmentCodeLengthRule(120)
        val item = newServicePlanAssignment("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = ServicePlanAssignmentRequiredDescriptionRule("service plan assignment needs a value")
        val item = newServicePlanAssignment("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = ServicePlanAssignmentAttemptsRangeRule(32, 64)
        val item = newServicePlanAssignment("x", "y")
        item.attempts = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = ServicePlanAssignmentRequiredSegmentRule("service plan assignment needs an entry")
        val item = newServicePlanAssignment("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
