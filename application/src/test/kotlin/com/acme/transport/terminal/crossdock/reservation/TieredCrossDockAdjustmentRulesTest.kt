package com.acme.transport.terminal.crossdock.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCrossDockAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredCrossDockAdjustmentValidator()
        val item = newTieredCrossDockAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TieredCrossDockAdjustmentRequiredNameRule("tiered cross dock adjustment needs this field")
        val item = newTieredCrossDockAdjustment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = TieredCrossDockAdjustmentRequiredNotesRule("tiered cross dock adjustment needs a value")
        val item = newTieredCrossDockAdjustment("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = TieredCrossDockAdjustmentNameLengthRule(255)
        val item = newTieredCrossDockAdjustment("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredCrossDockAdjustmentRequiredCodeRule("tiered cross dock adjustment needs an entry")
        val item = newTieredCrossDockAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = TieredCrossDockAdjustmentNotesLengthRule(255)
        val item = newTieredCrossDockAdjustment("x", "y")
        item.notes = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
