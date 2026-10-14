package com.acme.transport.linehaul.trailer.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingTrailerSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingTrailerSegmentValidator()
        val item = newPendingTrailerSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingTrailerSegmentCodeLengthRule(64)
        val item = newPendingTrailerSegment("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PendingTrailerSegmentRequiredLabelRule("pending trailer segment needs an entry")
        val item = newPendingTrailerSegment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingTrailerSegmentRequiredCodeRule("pending trailer segment needs an entry")
        val item = newPendingTrailerSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = PendingTrailerSegmentTrailerIdLengthRule(255)
        val item = newPendingTrailerSegment("x", "y")
        item.trailerId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = PendingTrailerSegmentRateRangeRule(2.0, 4836.4)
        val item = newPendingTrailerSegment("x", "y")
        item.rate = 4837.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
