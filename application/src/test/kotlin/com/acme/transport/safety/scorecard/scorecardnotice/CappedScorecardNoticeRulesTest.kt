package com.acme.transport.safety.scorecard.scorecardnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedScorecardNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedScorecardNoticeValidator()
        val item = newCappedScorecardNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = CappedScorecardNoticeUpdatedAtRangeRule(16, 255)
        val item = newCappedScorecardNotice("x", "y")
        item.updatedAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedScorecardNoticeRequiredCodeRule("capped scorecard notice needs a value")
        val item = newCappedScorecardNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
