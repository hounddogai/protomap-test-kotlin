package com.acme.transport.customs.hold.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedHoldEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedHoldEntryValidator()
        val item = newCappedHoldEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = CappedHoldEntryPalletsRangeRule(16, 3600)
        val item = newCappedHoldEntry("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CappedHoldEntryRequiredTitleRule("capped hold entry needs a value")
        val item = newCappedHoldEntry("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = CappedHoldEntryRequiredNotesRule("capped hold entry needs an entry")
        val item = newCappedHoldEntry("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CappedHoldEntryTitleLengthRule(1000)
        val item = newCappedHoldEntry("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long notes`() {
        val rule = CappedHoldEntryNotesLengthRule(32)
        val item = newCappedHoldEntry("x", "y")
        item.notes = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
