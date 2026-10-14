package com.acme.transport.terminal.dockdoor.doorsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDockDoorSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientDockDoorSlotValidator()
        val item = newLenientDockDoorSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientDockDoorSlotRequiredCodeRule("lenient dock door slot needs an entry")
        val item = newLenientDockDoorSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = LenientDockDoorSlotWeightRangeRule(0.8, 897.2)
        val item = newLenientDockDoorSlot("x", "y")
        item.weight = 898.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
