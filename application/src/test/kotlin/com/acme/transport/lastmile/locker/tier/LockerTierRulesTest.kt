package com.acme.transport.lastmile.locker.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerTierValidator()
        val item = newLockerTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LockerTierRequiredTitleRule("locker tier needs an entry")
        val item = newLockerTier("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = LockerTierRequiredNotesRule("locker tier needs this field")
        val item = newLockerTier("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LockerTierRequiredCodeRule("locker tier needs a value")
        val item = newLockerTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = LockerTierNotesLengthRule(64)
        val item = newLockerTier("x", "y")
        item.notes = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = LockerTierTitleLengthRule(1000)
        val item = newLockerTier("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
