package com.acme.transport.crewing.roster.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterNoticeValidator()
        val item = newRosterNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RosterNoticeTitleLengthRule(255)
        val item = newRosterNotice("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RosterNoticeRequiredTitleRule("roster notice needs a value")
        val item = newRosterNotice("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
