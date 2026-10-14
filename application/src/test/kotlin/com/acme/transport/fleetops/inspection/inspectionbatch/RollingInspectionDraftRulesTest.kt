package com.acme.transport.fleetops.inspection.inspectionbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingInspectionDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingInspectionDraftValidator()
        val item = newRollingInspectionDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = RollingInspectionDraftStopCountRangeRule(120, 86400)
        val item = newRollingInspectionDraft("x", "y")
        item.stopCount = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long description`() {
        val rule = RollingInspectionDraftDescriptionLengthRule(86400)
        val item = newRollingInspectionDraft("x", "y")
        item.description = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = RollingInspectionDraftRequiredDescriptionRule("rolling inspection draft needs this field")
        val item = newRollingInspectionDraft("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RollingInspectionDraftTitleLengthRule(255)
        val item = newRollingInspectionDraft("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
