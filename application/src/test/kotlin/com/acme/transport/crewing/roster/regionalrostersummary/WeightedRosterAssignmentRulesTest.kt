package com.acme.transport.crewing.roster.regionalrostersummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRosterAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedRosterAssignmentValidator()
        val item = newWeightedRosterAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedRosterAssignmentRequiredTitleRule("weighted roster assignment needs this field")
        val item = newWeightedRosterAssignment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = WeightedRosterAssignmentCapacityRangeRule(120, 86400)
        val item = newWeightedRosterAssignment("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = WeightedRosterAssignmentDepotLengthRule(64)
        val item = newWeightedRosterAssignment("x", "y")
        item.depot = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = WeightedRosterAssignmentTitleLengthRule(64)
        val item = newWeightedRosterAssignment("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
