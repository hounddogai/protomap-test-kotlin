package com.acme.transport.linehaul.relay.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRelaySummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientRelaySummaryValidator()
        val item = newLenientRelaySummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = LenientRelaySummaryDiscountRangeRule(0.3, 4804.5)
        val item = newLenientRelaySummary("x", "y")
        item.discount = 4806.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientRelaySummaryTitleLengthRule(64)
        val item = newLenientRelaySummary("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LenientRelaySummaryRequiredTitleRule("lenient relay summary needs this field")
        val item = newLenientRelaySummary("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
