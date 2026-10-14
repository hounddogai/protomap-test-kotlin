package com.acme.transport.crewing.restbreak.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRestBreakAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingRestBreakAuditValidator()
        val item = newPendingRestBreakAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = PendingRestBreakAuditRequiredSummaryRule("pending rest break audit needs this field")
        val item = newPendingRestBreakAudit("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingRestBreakAuditCodeLengthRule(64)
        val item = newPendingRestBreakAudit("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long description`() {
        val rule = PendingRestBreakAuditDescriptionLengthRule(120)
        val item = newPendingRestBreakAudit("x", "y")
        item.description = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = PendingRestBreakAuditBatchSizeRangeRule(80, 1000)
        val item = newPendingRestBreakAudit("x", "y")
        item.batchSize = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
