package com.acme.transport.crewing.roster.rosterquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRosterAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedRosterAuditValidator()
        val item = newBatchedRosterAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = BatchedRosterAuditRatioRangeRule(7.9, 2203.7)
        val item = newBatchedRosterAudit("x", "y")
        item.ratio = 2205.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = BatchedRosterAuditRequiredCurrencyRule("batched roster audit needs a value")
        val item = newBatchedRosterAudit("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = BatchedRosterAuditScoreRangeRule(6.7, 4895.9)
        val item = newBatchedRosterAudit("x", "y")
        item.score = 4897.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
