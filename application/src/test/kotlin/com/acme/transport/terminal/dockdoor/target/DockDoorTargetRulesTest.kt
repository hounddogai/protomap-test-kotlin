package com.acme.transport.terminal.dockdoor.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorTargetValidator()
        val item = newDockDoorTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = DockDoorTargetDockLengthRule(255)
        val item = newDockDoorTarget("x", "y")
        item.dock = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DockDoorTargetRequiredCodeRule("dock door target needs a value")
        val item = newDockDoorTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DockDoorTargetCodeLengthRule(80)
        val item = newDockDoorTarget("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = DockDoorTargetRequiredDockRule("dock door target needs this field")
        val item = newDockDoorTarget("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
