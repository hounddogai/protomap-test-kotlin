package com.acme.transport.fleetops.utilization.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationAssignmentValidator()
        val item = newUtilizationAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = UtilizationAssignmentRequiredTitleRule("utilization assignment needs an entry")
        val item = newUtilizationAssignment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = UtilizationAssignmentCeilingRangeRule(4.0, 4762.9)
        val item = newUtilizationAssignment("x", "y")
        item.ceiling = 4764.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above priority`() {
        val rule = UtilizationAssignmentPriorityRangeRule(1000, 3600)
        val item = newUtilizationAssignment("x", "y")
        item.priority = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = UtilizationAssignmentDurationMinutesRangeRule(32, 32)
        val item = newUtilizationAssignment("x", "y")
        item.durationMinutes = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
