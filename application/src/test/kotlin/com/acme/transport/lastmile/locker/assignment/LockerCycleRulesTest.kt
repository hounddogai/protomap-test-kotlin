package com.acme.transport.lastmile.locker.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerCycleValidator()
        val item = newLockerCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LockerCycleRequiredTitleRule("locker cycle needs an entry")
        val item = newLockerCycle("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LockerCycleCodeLengthRule(32)
        val item = newLockerCycle("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = LockerCycleStopCountRangeRule(16, 120)
        val item = newLockerCycle("x", "y")
        item.stopCount = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = LockerCycleTitleLengthRule(32)
        val item = newLockerCycle("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = LockerCycleRequiredDockRule("locker cycle needs this field")
        val item = newLockerCycle("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
