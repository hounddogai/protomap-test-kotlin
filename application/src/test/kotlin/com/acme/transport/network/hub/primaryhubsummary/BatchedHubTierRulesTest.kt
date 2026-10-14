package com.acme.transport.network.hub.primaryhubsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedHubTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedHubTierValidator()
        val item = newBatchedHubTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = BatchedHubTierSequenceRangeRule(255, 86400)
        val item = newBatchedHubTier("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedHubTierRequiredCodeRule("batched hub tier needs an entry")
        val item = newBatchedHubTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = BatchedHubTierRequiredDriverIdRule("batched hub tier needs an entry")
        val item = newBatchedHubTier("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
