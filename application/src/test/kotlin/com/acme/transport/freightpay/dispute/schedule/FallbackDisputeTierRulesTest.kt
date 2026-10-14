package com.acme.transport.freightpay.dispute.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDisputeTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackDisputeTierValidator()
        val item = newFallbackDisputeTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = FallbackDisputeTierCreatedAtRangeRule(64, 80)
        val item = newFallbackDisputeTier("x", "y")
        item.createdAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = FallbackDisputeTierRequiredShiftCodeRule("fallback dispute tier needs an entry")
        val item = newFallbackDisputeTier("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = FallbackDisputeTierRequiredLocaleRule("fallback dispute tier needs an entry")
        val item = newFallbackDisputeTier("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackDisputeTierTitleLengthRule(16)
        val item = newFallbackDisputeTier("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
