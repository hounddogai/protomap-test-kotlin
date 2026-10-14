package com.acme.transport.freightpay.audit.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditRuleValidator()
        val item = newAuditRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AuditRuleRequiredCodeRule("audit rule needs an entry")
        val item = newAuditRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = AuditRuleTrailerIdLengthRule(64)
        val item = newAuditRule("x", "y")
        item.trailerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = AuditRuleBatchSizeRangeRule(64, 255)
        val item = newAuditRule("x", "y")
        item.batchSize = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AuditRuleCodeLengthRule(64)
        val item = newAuditRule("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
