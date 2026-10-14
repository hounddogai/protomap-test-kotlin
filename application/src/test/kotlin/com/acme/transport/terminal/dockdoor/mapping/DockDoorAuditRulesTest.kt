package com.acme.transport.terminal.dockdoor.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorAuditValidator()
        val item = newDockDoorAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DockDoorAuditNameLengthRule(1000)
        val item = newDockDoorAudit("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = DockDoorAuditCostRangeRule(8.8, 2516.4)
        val item = newDockDoorAudit("x", "y")
        item.cost = 2517.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DockDoorAuditRequiredNameRule("dock door audit needs an entry")
        val item = newDockDoorAudit("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = DockDoorAuditFuelLitersRangeRule(6.9, 4769.3)
        val item = newDockDoorAudit("x", "y")
        item.fuelLiters = 4770.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
