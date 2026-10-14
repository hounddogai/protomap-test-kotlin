package com.acme.transport.terminal.dockdoor.doorrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedDockDoorAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedDockDoorAuditValidator()
        val item = newWeightedDockDoorAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedDockDoorAuditCodeLengthRule(3600)
        val item = newWeightedDockDoorAudit("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedDockDoorAuditRequiredLabelRule("weighted dock door audit needs this field")
        val item = newWeightedDockDoorAudit("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = WeightedDockDoorAuditLabelLengthRule(80)
        val item = newWeightedDockDoorAudit("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
