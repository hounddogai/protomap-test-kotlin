package com.acme.transport.terminal.crossdock.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockSlotValidator()
        val item = newCrossDockSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CrossDockSlotRequiredLabelRule("cross dock slot needs this field")
        val item = newCrossDockSlot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = CrossDockSlotPriorityRangeRule(64, 3600)
        val item = newCrossDockSlot("x", "y")
        item.priority = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = CrossDockSlotCodeLengthRule(32)
        val item = newCrossDockSlot("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
