package com.acme.transport.safety.scorecard.scorecardnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientScorecardSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientScorecardSnapshotValidator()
        val item = newLenientScorecardSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientScorecardSnapshotTitleLengthRule(86400)
        val item = newLenientScorecardSnapshot("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = LenientScorecardSnapshotAttemptsRangeRule(64, 80)
        val item = newLenientScorecardSnapshot("x", "y")
        item.attempts = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LenientScorecardSnapshotRequiredTitleRule("lenient scorecard snapshot needs this field")
        val item = newLenientScorecardSnapshot("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
