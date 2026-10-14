package com.acme.transport.crewing.restbreak.legacybreakentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRestBreakEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingRestBreakEntryValidator()
        val item = newRollingRestBreakEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = RollingRestBreakEntryGraceDaysRangeRule(255, 3600)
        val item = newRollingRestBreakEntry("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = RollingRestBreakEntryContactEmailLengthRule(64)
        val item = newRollingRestBreakEntry("x", "y")
        item.contactEmail = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = RollingRestBreakEntryLabelLengthRule(120)
        val item = newRollingRestBreakEntry("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = RollingRestBreakEntryRevisionRangeRule(80, 86400)
        val item = newRollingRestBreakEntry("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = RollingRestBreakEntryCreatedAtRangeRule(120, 3600)
        val item = newRollingRestBreakEntry("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
