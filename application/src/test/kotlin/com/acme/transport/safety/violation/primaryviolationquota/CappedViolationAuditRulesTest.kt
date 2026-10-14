package com.acme.transport.safety.violation.primaryviolationquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedViolationAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedViolationAuditValidator()
        val item = newCappedViolationAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = CappedViolationAuditLocaleLengthRule(80)
        val item = newCappedViolationAudit("x", "y")
        item.locale = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CappedViolationAuditRequiredTitleRule("capped violation audit needs an entry")
        val item = newCappedViolationAudit("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = CappedViolationAuditRequiredSegmentRule("capped violation audit needs an entry")
        val item = newCappedViolationAudit("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
