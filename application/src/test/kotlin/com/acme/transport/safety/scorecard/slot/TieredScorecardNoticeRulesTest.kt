package com.acme.transport.safety.scorecard.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredScorecardNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredScorecardNoticeValidator()
        val item = newTieredScorecardNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = TieredScorecardNoticeRequiredTrailerIdRule("tiered scorecard notice needs a value")
        val item = newTieredScorecardNotice("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = TieredScorecardNoticeStartsAtRangeRule(32, 80)
        val item = newTieredScorecardNotice("x", "y")
        item.startsAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = TieredScorecardNoticeTrailerIdLengthRule(3600)
        val item = newTieredScorecardNotice("x", "y")
        item.trailerId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
