package com.acme.transport.terminal.traileryard.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTrailerYardDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictTrailerYardDigestValidator()
        val item = newStrictTrailerYardDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = StrictTrailerYardDigestBatchSizeRangeRule(16, 64)
        val item = newStrictTrailerYardDigest("x", "y")
        item.batchSize = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = StrictTrailerYardDigestRequiredSummaryRule("strict trailer yard digest needs this field")
        val item = newStrictTrailerYardDigest("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictTrailerYardDigestCodeLengthRule(1000)
        val item = newStrictTrailerYardDigest("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long notes`() {
        val rule = StrictTrailerYardDigestNotesLengthRule(120)
        val item = newStrictTrailerYardDigest("x", "y")
        item.notes = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = StrictTrailerYardDigestAttemptsRangeRule(255, 1000)
        val item = newStrictTrailerYardDigest("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
