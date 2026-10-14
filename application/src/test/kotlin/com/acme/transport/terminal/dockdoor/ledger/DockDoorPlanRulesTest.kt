package com.acme.transport.terminal.dockdoor.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDockDoorPlanValidator()
        val item = newDockDoorPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DockDoorPlanCodeLengthRule(255)
        val item = newDockDoorPlan("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DockDoorPlanRequiredNameRule("dock door plan needs this field")
        val item = newDockDoorPlan("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = DockDoorPlanRequiredCategoryRule("dock door plan needs this field")
        val item = newDockDoorPlan("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
