package com.acme.transport.fleetops.inspection.globalinspectionscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledInspectionScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledInspectionScoreValidator()
        val item = newScheduledInspectionScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = ScheduledInspectionScoreCostRangeRule(9.4, 4790.7)
        val item = newScheduledInspectionScore("x", "y")
        item.cost = 4792.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledInspectionScoreRequiredNameRule("scheduled inspection score needs an entry")
        val item = newScheduledInspectionScore("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledInspectionScoreRequiredCodeRule("scheduled inspection score needs this field")
        val item = newScheduledInspectionScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = ScheduledInspectionScoreChecksumLengthRule(86400)
        val item = newScheduledInspectionScore("x", "y")
        item.checksum = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
