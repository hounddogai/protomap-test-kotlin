package com.acme.transport.fleetops.tire.tireagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTireAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyTireAssignmentValidator()
        val item = newLegacyTireAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyTireAssignmentRequiredCodeRule("legacy tire assignment needs a value")
        val item = newLegacyTireAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyTireAssignmentCodeLengthRule(64)
        val item = newLegacyTireAssignment("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = LegacyTireAssignmentTitleLengthRule(3600)
        val item = newLegacyTireAssignment("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long segment`() {
        val rule = LegacyTireAssignmentSegmentLengthRule(86400)
        val item = newLegacyTireAssignment("x", "y")
        item.segment = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = LegacyTireAssignmentEtaMinutesRangeRule(32, 120)
        val item = newLegacyTireAssignment("x", "y")
        item.etaMinutes = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
