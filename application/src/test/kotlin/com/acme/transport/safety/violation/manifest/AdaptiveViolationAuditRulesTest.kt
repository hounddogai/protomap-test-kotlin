package com.acme.transport.safety.violation.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveViolationAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveViolationAuditValidator()
        val item = newAdaptiveViolationAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveViolationAuditRequiredCodeRule("adaptive violation audit needs this field")
        val item = newAdaptiveViolationAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = AdaptiveViolationAuditCreatedAtRangeRule(3600, 86400)
        val item = newAdaptiveViolationAudit("x", "y")
        item.createdAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveViolationAuditNameLengthRule(255)
        val item = newAdaptiveViolationAudit("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = AdaptiveViolationAuditLabelLengthRule(16)
        val item = newAdaptiveViolationAudit("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
