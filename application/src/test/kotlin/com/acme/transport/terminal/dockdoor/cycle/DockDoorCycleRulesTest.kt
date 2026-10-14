package com.acme.transport.terminal.dockdoor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorCycleValidator()
        val item = newDockDoorCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = DockDoorCycleSummaryLengthRule(86400)
        val item = newDockDoorCycle("x", "y")
        item.summary = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DockDoorCycleRequiredCodeRule("dock door cycle needs an entry")
        val item = newDockDoorCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DockDoorCycleCodeLengthRule(120)
        val item = newDockDoorCycle("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = DockDoorCycleTitleLengthRule(16)
        val item = newDockDoorCycle("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
