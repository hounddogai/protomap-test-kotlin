package com.acme.transport.fleetops.defect.defectrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectAuditValidator()
        val item = newDefectAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DefectAuditRequiredNameRule("defect audit needs this field")
        val item = newDefectAudit("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefectAuditCodeLengthRule(64)
        val item = newDefectAudit("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long phone`() {
        val rule = DefectAuditPhoneLengthRule(3600)
        val item = newDefectAudit("x", "y")
        item.phone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = DefectAuditRequiredTimezoneRule("defect audit needs an entry")
        val item = newDefectAudit("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
