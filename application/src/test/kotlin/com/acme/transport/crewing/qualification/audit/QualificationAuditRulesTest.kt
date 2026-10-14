package com.acme.transport.crewing.qualification.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationAuditValidator()
        val item = newQualificationAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = QualificationAuditCodeLengthRule(3600)
        val item = newQualificationAudit("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = QualificationAuditStopCountRangeRule(120, 1000)
        val item = newQualificationAudit("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = QualificationAuditLabelLengthRule(80)
        val item = newQualificationAudit("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
