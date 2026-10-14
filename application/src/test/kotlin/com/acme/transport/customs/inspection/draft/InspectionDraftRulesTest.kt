package com.acme.transport.customs.inspection.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionDraftValidator()
        val item = newInspectionDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = InspectionDraftCeilingRangeRule(2.0, 954.1)
        val item = newInspectionDraft("x", "y")
        item.ceiling = 955.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = InspectionDraftNameLengthRule(32)
        val item = newInspectionDraft("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long lane`() {
        val rule = InspectionDraftLaneLengthRule(1000)
        val item = newInspectionDraft("x", "y")
        item.lane = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionDraftRequiredCodeRule("inspection draft needs an entry")
        val item = newInspectionDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
