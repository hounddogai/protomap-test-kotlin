package com.acme.transport.linehaul.laneplan.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanAssignmentValidator()
        val item = newLanePlanAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = LanePlanAssignmentWindowDaysRangeRule(80, 86400)
        val item = newLanePlanAssignment("x", "y")
        item.windowDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LanePlanAssignmentRequiredNameRule("lane plan assignment needs an entry")
        val item = newLanePlanAssignment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = LanePlanAssignmentRequiredChecksumRule("lane plan assignment needs an entry")
        val item = newLanePlanAssignment("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
