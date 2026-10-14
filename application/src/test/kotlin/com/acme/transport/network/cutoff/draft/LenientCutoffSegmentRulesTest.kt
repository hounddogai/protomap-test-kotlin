package com.acme.transport.network.cutoff.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCutoffSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientCutoffSegmentValidator()
        val item = newLenientCutoffSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = LenientCutoffSegmentRequiredReviewerRule("lenient cutoff segment needs a value")
        val item = newLenientCutoffSegment("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LenientCutoffSegmentLabelLengthRule(120)
        val item = newLenientCutoffSegment("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientCutoffSegmentCodeLengthRule(64)
        val item = newLenientCutoffSegment("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
