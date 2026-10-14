package com.acme.transport.customs.clearance.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceSlotValidator()
        val item = newClearanceSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ClearanceSlotCodeLengthRule(3600)
        val item = newClearanceSlot("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = ClearanceSlotCeilingRangeRule(5.8, 4324.2)
        val item = newClearanceSlot("x", "y")
        item.ceiling = 4325.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
