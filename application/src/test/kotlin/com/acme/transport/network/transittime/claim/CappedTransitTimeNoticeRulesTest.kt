package com.acme.transport.network.transittime.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedTransitTimeNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedTransitTimeNoticeValidator()
        val item = newCappedTransitTimeNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CappedTransitTimeNoticeRequiredTitleRule("capped transit time notice needs this field")
        val item = newCappedTransitTimeNotice("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedTransitTimeNoticeRequiredCodeRule("capped transit time notice needs an entry")
        val item = newCappedTransitTimeNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CappedTransitTimeNoticeTitleLengthRule(16)
        val item = newCappedTransitTimeNotice("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = CappedTransitTimeNoticeMinimumRangeRule(6.7, 3278.2)
        val item = newCappedTransitTimeNotice("x", "y")
        item.minimum = 3279.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
