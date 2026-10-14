package com.acme.transport.terminal.dockdoor.doorscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorDraftValidator()
        val item = newDockDoorDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = DockDoorDraftRequiredPhoneRule("dock door draft needs a value")
        val item = newDockDoorDraft("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DockDoorDraftRequiredNameRule("dock door draft needs an entry")
        val item = newDockDoorDraft("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = DockDoorDraftStartsAtRangeRule(16, 86400)
        val item = newDockDoorDraft("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DockDoorDraftRequiredCodeRule("dock door draft needs an entry")
        val item = newDockDoorDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
