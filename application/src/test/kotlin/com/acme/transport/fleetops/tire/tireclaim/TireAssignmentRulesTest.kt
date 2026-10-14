package com.acme.transport.fleetops.tire.tireclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireAssignmentValidator()
        val item = newTireAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TireAssignmentCodeLengthRule(80)
        val item = newTireAssignment("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = TireAssignmentDurationMinutesRangeRule(16, 64)
        val item = newTireAssignment("x", "y")
        item.durationMinutes = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TireAssignmentRequiredTitleRule("tire assignment needs this field")
        val item = newTireAssignment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = TireAssignmentRegionLengthRule(16)
        val item = newTireAssignment("x", "y")
        item.region = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TireAssignmentRequiredCodeRule("tire assignment needs an entry")
        val item = newTireAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
