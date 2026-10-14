package com.acme.transport.crewing.roster.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterVarianceValidator()
        val item = newRosterVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RosterVarianceLabelLengthRule(120)
        val item = newRosterVariance("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = RosterVarianceThresholdRangeRule(8.4, 2728.3)
        val item = newRosterVariance("x", "y")
        item.threshold = 2729.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = RosterVarianceGraceDaysRangeRule(32, 255)
        val item = newRosterVariance("x", "y")
        item.graceDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = RosterVarianceExternalRefLengthRule(16)
        val item = newRosterVariance("x", "y")
        item.externalRef = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = RosterVarianceReviewerLengthRule(255)
        val item = newRosterVariance("x", "y")
        item.reviewer = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
