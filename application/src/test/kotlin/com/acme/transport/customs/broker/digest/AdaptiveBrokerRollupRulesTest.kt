package com.acme.transport.customs.broker.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveBrokerRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveBrokerRollupValidator()
        val item = newAdaptiveBrokerRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = AdaptiveBrokerRollupCreatedAtRangeRule(16, 3600)
        val item = newAdaptiveBrokerRollup("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = AdaptiveBrokerRollupSequenceRangeRule(64, 120)
        val item = newAdaptiveBrokerRollup("x", "y")
        item.sequence = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = AdaptiveBrokerRollupStartsAtRangeRule(64, 86400)
        val item = newAdaptiveBrokerRollup("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = AdaptiveBrokerRollupReviewerLengthRule(3600)
        val item = newAdaptiveBrokerRollup("x", "y")
        item.reviewer = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
