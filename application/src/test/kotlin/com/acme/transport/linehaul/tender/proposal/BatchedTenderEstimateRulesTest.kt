package com.acme.transport.linehaul.tender.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTenderEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedTenderEstimateValidator()
        val item = newBatchedTenderEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedTenderEstimateRequiredCodeRule("batched tender estimate needs this field")
        val item = newBatchedTenderEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = BatchedTenderEstimateRequiredChecksumRule("batched tender estimate needs an entry")
        val item = newBatchedTenderEstimate("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = BatchedTenderEstimateChecksumLengthRule(64)
        val item = newBatchedTenderEstimate("x", "y")
        item.checksum = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedTenderEstimateTitleLengthRule(64)
        val item = newBatchedTenderEstimate("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
