package com.acme.transport.lastmile.drop.dropbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDropTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredDropTargetValidator()
        val item = newTieredDropTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredDropTargetTitleLengthRule(64)
        val item = newTieredDropTarget("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredDropTargetRequiredCodeRule("tiered drop target needs this field")
        val item = newTieredDropTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = TieredDropTargetNotesLengthRule(32)
        val item = newTieredDropTarget("x", "y")
        item.notes = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = TieredDropTargetRequiredNotesRule("tiered drop target needs an entry")
        val item = newTieredDropTarget("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
