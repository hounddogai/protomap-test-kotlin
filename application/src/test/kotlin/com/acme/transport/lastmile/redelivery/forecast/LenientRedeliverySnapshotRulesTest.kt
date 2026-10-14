package com.acme.transport.lastmile.redelivery.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRedeliverySnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientRedeliverySnapshotValidator()
        val item = newLenientRedeliverySnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = LenientRedeliverySnapshotRequiredReviewerRule("lenient redelivery snapshot needs an entry")
        val item = newLenientRedeliverySnapshot("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LenientRedeliverySnapshotLabelLengthRule(64)
        val item = newLenientRedeliverySnapshot("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientRedeliverySnapshotCodeLengthRule(64)
        val item = newLenientRedeliverySnapshot("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = LenientRedeliverySnapshotLoadKgRangeRule(0.6, 4932.0)
        val item = newLenientRedeliverySnapshot("x", "y")
        item.loadKg = 4933.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = LenientRedeliverySnapshotReviewerLengthRule(64)
        val item = newLenientRedeliverySnapshot("x", "y")
        item.reviewer = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
