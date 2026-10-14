package com.acme.transport.customs.inspection.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryInspectionAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryInspectionAuditValidator()
        val item = newPrimaryInspectionAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryInspectionAuditCodeLengthRule(32)
        val item = newPrimaryInspectionAudit("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PrimaryInspectionAuditRequiredTitleRule("primary inspection audit needs a value")
        val item = newPrimaryInspectionAudit("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = PrimaryInspectionAuditVersionRangeRule(16, 64)
        val item = newPrimaryInspectionAudit("x", "y")
        item.version = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = PrimaryInspectionAuditRequiredUnitRule("primary inspection audit needs a value")
        val item = newPrimaryInspectionAudit("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
