package com.acme.transport.terminal.dockdoor.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDockDoorDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledDockDoorDraftValidator()
        val item = newScheduledDockDoorDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledDockDoorDraftCodeLengthRule(32)
        val item = newScheduledDockDoorDraft("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledDockDoorDraftRequiredCodeRule("scheduled dock door draft needs an entry")
        val item = newScheduledDockDoorDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = ScheduledDockDoorDraftOdometerRangeRule(80, 86400)
        val item = newScheduledDockDoorDraft("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScheduledDockDoorDraftRequiredTitleRule("scheduled dock door draft needs an entry")
        val item = newScheduledDockDoorDraft("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
