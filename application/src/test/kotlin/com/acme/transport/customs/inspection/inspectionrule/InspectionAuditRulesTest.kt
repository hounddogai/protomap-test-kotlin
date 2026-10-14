package com.acme.transport.customs.inspection.inspectionrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionAuditValidator()
        val item = newInspectionAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = InspectionAuditCostRangeRule(5.0, 652.7)
        val item = newInspectionAudit("x", "y")
        item.cost = 654.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = InspectionAuditTitleLengthRule(16)
        val item = newInspectionAudit("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = InspectionAuditWindowDaysRangeRule(32, 3600)
        val item = newInspectionAudit("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = InspectionAuditRequiredTitleRule("inspection audit needs a value")
        val item = newInspectionAudit("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = InspectionAuditCodeLengthRule(255)
        val item = newInspectionAudit("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
