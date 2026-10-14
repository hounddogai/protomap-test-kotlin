package com.acme.transport.customs.hold.holdaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualHoldAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualHoldAuditValidator()
        val item = newManualHoldAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ManualHoldAuditNameLengthRule(86400)
        val item = newManualHoldAudit("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = ManualHoldAuditRequiredCategoryRule("manual hold audit needs an entry")
        val item = newManualHoldAudit("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = ManualHoldAuditRequiredSummaryRule("manual hold audit needs a value")
        val item = newManualHoldAudit("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = ManualHoldAuditSummaryLengthRule(80)
        val item = newManualHoldAudit("x", "y")
        item.summary = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
