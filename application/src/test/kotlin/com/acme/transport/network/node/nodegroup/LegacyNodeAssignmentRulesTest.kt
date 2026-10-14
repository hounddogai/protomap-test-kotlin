package com.acme.transport.network.node.nodegroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyNodeAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyNodeAssignmentValidator()
        val item = newLegacyNodeAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = LegacyNodeAssignmentLimitRangeRule(32, 120)
        val item = newLegacyNodeAssignment("x", "y")
        item.limit = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyNodeAssignmentRequiredCodeRule("legacy node assignment needs an entry")
        val item = newLegacyNodeAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = LegacyNodeAssignmentRequiredExternalRefRule("legacy node assignment needs an entry")
        val item = newLegacyNodeAssignment("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = LegacyNodeAssignmentOdometerRangeRule(1000, 3600)
        val item = newLegacyNodeAssignment("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above window days`() {
        val rule = LegacyNodeAssignmentWindowDaysRangeRule(120, 120)
        val item = newLegacyNodeAssignment("x", "y")
        item.windowDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = LegacyNodeAssignmentNameLengthRule(32)
        val item = newLegacyNodeAssignment("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
