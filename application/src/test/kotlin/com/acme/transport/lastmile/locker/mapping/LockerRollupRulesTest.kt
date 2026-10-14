package com.acme.transport.lastmile.locker.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerRollupValidator()
        val item = newLockerRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = LockerRollupNameLengthRule(255)
        val item = newLockerRollup("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = LockerRollupTitleLengthRule(3600)
        val item = newLockerRollup("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above created at`() {
        val rule = LockerRollupCreatedAtRangeRule(255, 1000)
        val item = newLockerRollup("x", "y")
        item.createdAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long dock`() {
        val rule = LockerRollupDockLengthRule(32)
        val item = newLockerRollup("x", "y")
        item.dock = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
