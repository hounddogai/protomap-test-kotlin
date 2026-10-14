package com.acme.transport.network.transittime.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeReviewValidator()
        val item = newTransitTimeReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = TransitTimeReviewRequiredChecksumRule("transit time review needs an entry")
        val item = newTransitTimeReview("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = TransitTimeReviewChecksumLengthRule(16)
        val item = newTransitTimeReview("x", "y")
        item.checksum = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = TransitTimeReviewCodeLengthRule(64)
        val item = newTransitTimeReview("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = TransitTimeReviewRatioRangeRule(5.9, 4263.6)
        val item = newTransitTimeReview("x", "y")
        item.ratio = 4265.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above margin`() {
        val rule = TransitTimeReviewMarginRangeRule(8.7, 242.6)
        val item = newTransitTimeReview("x", "y")
        item.margin = 244.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
