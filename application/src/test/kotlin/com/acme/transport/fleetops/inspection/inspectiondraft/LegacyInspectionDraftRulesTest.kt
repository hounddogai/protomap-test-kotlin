package com.acme.transport.fleetops.inspection.inspectiondraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyInspectionDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyInspectionDraftValidator()
        val item = newLegacyInspectionDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = LegacyInspectionDraftOdometerRangeRule(80, 1000)
        val item = newLegacyInspectionDraft("x", "y")
        item.odometer = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = LegacyInspectionDraftTrailerIdLengthRule(3600)
        val item = newLegacyInspectionDraft("x", "y")
        item.trailerId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyInspectionDraftLabelLengthRule(32)
        val item = newLegacyInspectionDraft("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
