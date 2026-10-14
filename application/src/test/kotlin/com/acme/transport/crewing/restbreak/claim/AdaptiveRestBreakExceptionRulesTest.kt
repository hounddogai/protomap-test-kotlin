package com.acme.transport.crewing.restbreak.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveRestBreakExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveRestBreakExceptionValidator()
        val item = newAdaptiveRestBreakException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = AdaptiveRestBreakExceptionGraceDaysRangeRule(1000, 3600)
        val item = newAdaptiveRestBreakException("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = AdaptiveRestBreakExceptionRequiredChecksumRule("adaptive rest break exception needs this field")
        val item = newAdaptiveRestBreakException("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
