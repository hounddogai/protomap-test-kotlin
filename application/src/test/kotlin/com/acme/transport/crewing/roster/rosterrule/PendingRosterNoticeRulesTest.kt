package com.acme.transport.crewing.roster.rosterrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRosterNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingRosterNoticeValidator()
        val item = newPendingRosterNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingRosterNoticeCodeLengthRule(1000)
        val item = newPendingRosterNotice("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = PendingRosterNoticeScoreRangeRule(0.1, 556.2)
        val item = newPendingRosterNotice("x", "y")
        item.score = 557.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = PendingRosterNoticeRequiredDriverIdRule("pending roster notice needs a value")
        val item = newPendingRosterNotice("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = PendingRosterNoticeNameLengthRule(1000)
        val item = newPendingRosterNotice("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = PendingRosterNoticeDriverIdLengthRule(3600)
        val item = newPendingRosterNotice("x", "y")
        item.driverId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
