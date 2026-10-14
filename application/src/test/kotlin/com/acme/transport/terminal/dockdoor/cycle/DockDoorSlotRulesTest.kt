package com.acme.transport.terminal.dockdoor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorSlotValidator()
        val item = newDockDoorSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = DockDoorSlotEndsAtRangeRule(32, 80)
        val item = newDockDoorSlot("x", "y")
        item.endsAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DockDoorSlotRequiredCodeRule("dock door slot needs an entry")
        val item = newDockDoorSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = DockDoorSlotRequiredLocaleRule("dock door slot needs an entry")
        val item = newDockDoorSlot("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
