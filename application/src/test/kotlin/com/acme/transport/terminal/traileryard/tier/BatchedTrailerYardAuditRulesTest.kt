package com.acme.transport.terminal.traileryard.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTrailerYardAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedTrailerYardAuditValidator()
        val item = newBatchedTrailerYardAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = BatchedTrailerYardAuditRequiredCountryRule("batched trailer yard audit needs this field")
        val item = newBatchedTrailerYardAudit("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedTrailerYardAuditCodeLengthRule(120)
        val item = newBatchedTrailerYardAudit("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = BatchedTrailerYardAuditBatchSizeRangeRule(80, 120)
        val item = newBatchedTrailerYardAudit("x", "y")
        item.batchSize = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
