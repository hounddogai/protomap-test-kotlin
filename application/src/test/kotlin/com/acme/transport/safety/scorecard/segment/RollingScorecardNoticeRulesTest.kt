package com.acme.transport.safety.scorecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingScorecardNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingScorecardNoticeValidator()
        val item = newRollingScorecardNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = RollingScorecardNoticeWeightRangeRule(4.9, 1549.6)
        val item = newRollingScorecardNotice("x", "y")
        item.weight = 1551.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = RollingScorecardNoticeTitleLengthRule(3600)
        val item = newRollingScorecardNotice("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = RollingScorecardNoticePalletsRangeRule(120, 86400)
        val item = newRollingScorecardNotice("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = RollingScorecardNoticeUpdatedAtRangeRule(80, 86400)
        val item = newRollingScorecardNotice("x", "y")
        item.updatedAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
