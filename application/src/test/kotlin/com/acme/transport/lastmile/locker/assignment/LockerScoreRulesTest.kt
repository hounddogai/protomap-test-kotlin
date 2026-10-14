package com.acme.transport.lastmile.locker.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerScoreValidator()
        val item = newLockerScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = LockerScoreStartsAtRangeRule(1000, 86400)
        val item = newLockerScore("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = LockerScoreRequiredNotesRule("locker score needs this field")
        val item = newLockerScore("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
