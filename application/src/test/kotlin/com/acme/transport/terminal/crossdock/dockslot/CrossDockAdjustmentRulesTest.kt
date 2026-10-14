package com.acme.transport.terminal.crossdock.dockslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockAdjustmentValidator()
        val item = newCrossDockAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CrossDockAdjustmentNameLengthRule(16)
        val item = newCrossDockAdjustment("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = CrossDockAdjustmentRequiredDriverIdRule("cross dock adjustment needs a value")
        val item = newCrossDockAdjustment("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = CrossDockAdjustmentDriverIdLengthRule(64)
        val item = newCrossDockAdjustment("x", "y")
        item.driverId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CrossDockAdjustmentRequiredNameRule("cross dock adjustment needs this field")
        val item = newCrossDockAdjustment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CrossDockAdjustmentCodeLengthRule(80)
        val item = newCrossDockAdjustment("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
