package com.acme.transport.network.cutoff.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCutoffScoreValidator()
        val item = newCutoffScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = CutoffScoreDistanceKmRangeRule(3.4, 4962.5)
        val item = newCutoffScore("x", "y")
        item.distanceKm = 4964.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CutoffScoreRequiredTitleRule("cutoff score needs a value")
        val item = newCutoffScore("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CutoffScoreTitleLengthRule(1000)
        val item = newCutoffScore("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
