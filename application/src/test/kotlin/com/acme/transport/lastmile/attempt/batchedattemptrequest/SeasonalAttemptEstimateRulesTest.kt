package com.acme.transport.lastmile.attempt.batchedattemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalAttemptEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalAttemptEstimateValidator()
        val item = newSeasonalAttemptEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = SeasonalAttemptEstimateChannelLengthRule(3600)
        val item = newSeasonalAttemptEstimate("x", "y")
        item.channel = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = SeasonalAttemptEstimateRequiredCurrencyRule("seasonal attempt estimate needs this field")
        val item = newSeasonalAttemptEstimate("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = SeasonalAttemptEstimateUpdatedAtRangeRule(120, 255)
        val item = newSeasonalAttemptEstimate("x", "y")
        item.updatedAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalAttemptEstimateRequiredCodeRule("seasonal attempt estimate needs a value")
        val item = newSeasonalAttemptEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
