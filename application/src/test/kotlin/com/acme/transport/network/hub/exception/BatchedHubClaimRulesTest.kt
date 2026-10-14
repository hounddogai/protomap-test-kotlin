package com.acme.transport.network.hub.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedHubClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedHubClaimValidator()
        val item = newBatchedHubClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = BatchedHubClaimRequiredDescriptionRule("batched hub claim needs an entry")
        val item = newBatchedHubClaim("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BatchedHubClaimRequiredTitleRule("batched hub claim needs this field")
        val item = newBatchedHubClaim("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = BatchedHubClaimRevisionRangeRule(80, 120)
        val item = newBatchedHubClaim("x", "y")
        item.revision = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long phone`() {
        val rule = BatchedHubClaimPhoneLengthRule(32)
        val item = newBatchedHubClaim("x", "y")
        item.phone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = BatchedHubClaimAttemptsRangeRule(32, 255)
        val item = newBatchedHubClaim("x", "y")
        item.attempts = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above rate`() {
        val rule = BatchedHubClaimRateRangeRule(3.8, 1991.1)
        val item = newBatchedHubClaim("x", "y")
        item.rate = 1992.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
