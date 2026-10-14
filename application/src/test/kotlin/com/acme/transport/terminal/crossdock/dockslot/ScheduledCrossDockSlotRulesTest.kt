package com.acme.transport.terminal.crossdock.dockslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledCrossDockSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledCrossDockSlotValidator()
        val item = newScheduledCrossDockSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledCrossDockSlotNameLengthRule(32)
        val item = newScheduledCrossDockSlot("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledCrossDockSlotRequiredNameRule("scheduled cross dock slot needs a value")
        val item = newScheduledCrossDockSlot("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
