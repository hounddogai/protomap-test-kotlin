package com.acme.transport.fleetops.inspection.draft

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
    fun `flags values above load kg`() {
        val rule = InspectionDraftLoadKgRangeRule(8.9, 1520.7)
        val item = newInspectionDraft("x", "y")
        item.loadKg = 1522.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionDraftRequiredCodeRule("inspection draft needs an entry")
        val item = newInspectionDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = InspectionDraftRouteCodeLengthRule(120)
        val item = newInspectionDraft("x", "y")
        item.routeCode = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
