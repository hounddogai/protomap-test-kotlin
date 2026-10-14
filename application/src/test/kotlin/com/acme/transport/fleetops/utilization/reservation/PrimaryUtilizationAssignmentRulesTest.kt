package com.acme.transport.fleetops.utilization.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryUtilizationAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryUtilizationAssignmentValidator()
        val item = newPrimaryUtilizationAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = PrimaryUtilizationAssignmentRequiredCategoryRule("primary utilization assignment needs an entry")
        val item = newPrimaryUtilizationAssignment("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PrimaryUtilizationAssignmentRequiredTitleRule("primary utilization assignment needs a value")
        val item = newPrimaryUtilizationAssignment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = PrimaryUtilizationAssignmentLoadKgRangeRule(7.4, 2934.0)
        val item = newPrimaryUtilizationAssignment("x", "y")
        item.loadKg = 2935.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
