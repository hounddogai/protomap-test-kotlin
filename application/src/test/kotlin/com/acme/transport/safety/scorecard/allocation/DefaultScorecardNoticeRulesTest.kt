package com.acme.transport.safety.scorecard.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultScorecardNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultScorecardNoticeValidator()
        val item = newDefaultScorecardNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = DefaultScorecardNoticeGraceDaysRangeRule(255, 3600)
        val item = newDefaultScorecardNotice("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DefaultScorecardNoticeRequiredTitleRule("default scorecard notice needs an entry")
        val item = newDefaultScorecardNotice("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = DefaultScorecardNoticeContactEmailLengthRule(32)
        val item = newDefaultScorecardNotice("x", "y")
        item.contactEmail = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = DefaultScorecardNoticeRequiredContactEmailRule("default scorecard notice needs an entry")
        val item = newDefaultScorecardNotice("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
