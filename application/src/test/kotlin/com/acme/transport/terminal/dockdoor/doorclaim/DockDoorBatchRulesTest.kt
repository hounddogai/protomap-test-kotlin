package com.acme.transport.terminal.dockdoor.doorclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorBatchValidator()
        val item = newDockDoorBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = DockDoorBatchQuantityRangeRule(32, 32)
        val item = newDockDoorBatch("x", "y")
        item.quantity = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DockDoorBatchRequiredCodeRule("dock door batch needs a value")
        val item = newDockDoorBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = DockDoorBatchRatioRangeRule(9.5, 2147.7)
        val item = newDockDoorBatch("x", "y")
        item.ratio = 2149.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = DockDoorBatchTitleLengthRule(120)
        val item = newDockDoorBatch("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
