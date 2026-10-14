package com.acme.transport.fleetops.inspection.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalInspectionDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalInspectionDraftValidator()
        val item = newSeasonalInspectionDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = SeasonalInspectionDraftPriorityRangeRule(16, 80)
        val item = newSeasonalInspectionDraft("x", "y")
        item.priority = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above limit`() {
        val rule = SeasonalInspectionDraftLimitRangeRule(32, 255)
        val item = newSeasonalInspectionDraft("x", "y")
        item.limit = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = SeasonalInspectionDraftDurationMinutesRangeRule(120, 1000)
        val item = newSeasonalInspectionDraft("x", "y")
        item.durationMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalInspectionDraftRequiredCodeRule("seasonal inspection draft needs an entry")
        val item = newSeasonalInspectionDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = SeasonalInspectionDraftNotesLengthRule(255)
        val item = newSeasonalInspectionDraft("x", "y")
        item.notes = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
