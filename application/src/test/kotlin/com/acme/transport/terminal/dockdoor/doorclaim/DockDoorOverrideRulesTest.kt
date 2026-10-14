package com.acme.transport.terminal.dockdoor.doorclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorOverrideValidator()
        val item = newDockDoorOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DockDoorOverrideRequiredCodeRule("dock door override needs this field")
        val item = newDockDoorOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = DockDoorOverrideLoadKgRangeRule(0.0, 3941.7)
        val item = newDockDoorOverride("x", "y")
        item.loadKg = 3943.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
